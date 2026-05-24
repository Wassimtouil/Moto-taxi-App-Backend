package com.example.taximotoapp_backend.trajet.service;

import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.model.Client;
import com.example.taximotoapp_backend.User.model.User;
import com.example.taximotoapp_backend.User.repository.ChauffeurRepository;
import com.example.taximotoapp_backend.User.repository.UserRepository;
import com.example.taximotoapp_backend.model.enumClass.Availability;
import com.example.taximotoapp_backend.model.enumClass.TripStatus;
import com.example.taximotoapp_backend.trajet.dto.request.TrajetRequest;
import com.example.taximotoapp_backend.trajet.dto.response.ChauffeurStatResponse;
import com.example.taximotoapp_backend.trajet.mapper.TrajetMapper;
import com.example.taximotoapp_backend.trajet.model.Trajet;
import com.example.taximotoapp_backend.trajet.model.TrajetLocation;
import com.example.taximotoapp_backend.trajet.dto.request.TrajetPreviewRequest;
import com.example.taximotoapp_backend.trajet.dto.response.TrajetPreviewResponse;
import com.example.taximotoapp_backend.trajet.repository.TrajetRepository;
import com.example.taximotoapp_backend.trajet.dto.response.TrajetResponse;
import com.example.taximotoapp_backend.paiement.service.PaiementService;
import com.example.taximotoapp_backend.paiement.service.WalletService;
import com.example.taximotoapp_backend.paiement.model.Wallet;
import com.example.taximotoapp_backend.paiement.model.Paiement;
import com.example.taximotoapp_backend.paiement.repository.PaiementRepository;
import com.example.taximotoapp_backend.model.enumClass.PaiementStatus;
import com.example.taximotoapp_backend.model.enumClass.PaiementType;
import com.example.taximotoapp_backend.websocket.service.TimeoutService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrajetService {
    private final TrajetRepository trajetRepository;
    private final UserRepository userRepository;
    private final TrajetMapper trajetMapper;
    private final ChauffeurRepository chauffeurRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TimeoutService timeoutService;
    private final com.example.taximotoapp_backend.location.repository.LocationRepository locationRepository;
    private final MapboxService mapboxService;
    private final WalletService walletService;
    private final PaiementService paiementService;
    private final PaiementRepository paiementRepository;

    public TrajetResponse createTrajet(TrajetRequest trajetRequest){
        // recuperer user a travers le jwt
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        User client = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("client not found"));

        // mapping request -> entity
        Trajet trajet = trajetMapper.toEntity(trajetRequest);

        // Determine initial status: Scheduled if far in future, Created if immediate or starting soon
        TripStatus initialStatus = TripStatus.Created;
        if (trajetRequest.getScheduledAt() != null) {
            LocalDateTime now = LocalDateTime.now();
            // If scheduled more than 10 minutes away, use 'Scheduled' status (delayed dispatch)
            if (trajetRequest.getScheduledAt().isAfter(now.plusMinutes(10))) {
                initialStatus = TripStatus.Scheduled;
            }
        }

        // ---------------------------------------------------------
        // EARLY CHECK: Find drivers BEFORE doing Mapbox calls or DB saves
        // ---------------------------------------------------------
        List<Chauffeur> drivers = null;
        if (initialStatus != TripStatus.Scheduled) {
            System.out.println("⚡ Ride is starting SOON or NOW. Searching for drivers...");
            System.out.println("🎯 [DEBUG] preferredDriverId from request: " + trajetRequest.getPreferredDriverId());

            if (trajetRequest.getPreferredDriverId() != null) {
                System.out.println("🎯 Rider explicitly requested driver ID: " + trajetRequest.getPreferredDriverId());
                Chauffeur preferredDriver = chauffeurRepository.findById(trajetRequest.getPreferredDriverId())
                        .orElseThrow(() -> new RuntimeException("Le chauffeur sélectionné n'existe pas"));

                if (preferredDriver.getAvailability() != com.example.taximotoapp_backend.model.enumClass.Availability.TRUE) {
                    throw new RuntimeException("Le chauffeur sélectionné n'est plus disponible");
                }
                drivers = List.of(preferredDriver);
            } else {
                drivers = findDriversWithExpansion(
                        trajetRequest.getPickupLatitude(),
                        trajetRequest.getPickupLongitude(),
                        trajetRequest.getPreferredDriverGender()
                );
            }

            System.out.println("🚗 Found " + drivers.size() + " nearby available drivers.");

            if (drivers.isEmpty()) {
                System.out.println("❌ No drivers found for the ride.");
                throw new RuntimeException("Aucun chauffeur disponible");
            }
        }

        // --- Initialiser/Mettre Ã  jour la position du client dans la base ---
        com.example.taximotoapp_backend.location.model.Location clientLocation = client.getLocation();
        if (clientLocation == null) {
            clientLocation = new com.example.taximotoapp_backend.location.model.Location();
            clientLocation.setUser(client);
            client.setLocation(clientLocation);
        }
        clientLocation.setLatitude(trajetRequest.getPickupLatitude());
        clientLocation.setLongitude(trajetRequest.getPickupLongitude());
        locationRepository.save(clientLocation);

        // crÃ©er et attacher TrajetLocation
        TrajetLocation trajetLocation = new TrajetLocation();
        trajetLocation.setPickupLatitude(trajetRequest.getPickupLatitude());
        trajetLocation.setPickupLongitude(trajetRequest.getPickupLongitude());
        trajetLocation.setDestinationLatitude(trajetRequest.getDestinationLatitude());
        trajetLocation.setDestinationLongitude(trajetRequest.getDestinationLongitude());

        // --- Road Data — use preview-computed values if provided, else call Mapbox ---
        Double distanceKm;
        Integer durationMinutes;
        String encodedPolyline;
        Double price;

        if (trajetRequest.getDistanceKm() != null) {
            distanceKm = trajetRequest.getDistanceKm();
            durationMinutes = trajetRequest.getDurationMinutes();
            encodedPolyline = trajetRequest.getEncodedPolyline();
            price = trajetRequest.getPrice() != null ? trajetRequest.getPrice() : calculatePrice(distanceKm);
        } else {
            MapboxService.RouteDetails roadmap = mapboxService.getRouteDetails(
                    trajetRequest.getPickupLatitude(), trajetRequest.getPickupLongitude(),
                    trajetRequest.getDestinationLatitude(), trajetRequest.getDestinationLongitude()
            );
            distanceKm = roadmap.getDistanceKm();
            durationMinutes = roadmap.getDurationMinutes();
            encodedPolyline = roadmap.getEncodedPolyline();
            price = calculatePrice(distanceKm);
        }

        // --- Address Resolution ---
        String pAddress = trajetRequest.getPickupAddress();
        if (pAddress == null || pAddress.isEmpty()) {
            pAddress = mapboxService.reverseGeocode(
                    trajetRequest.getPickupLatitude(), trajetRequest.getPickupLongitude());
        }
        trajetLocation.setPickupAddress(pAddress);

        String dAddress = trajetRequest.getDestinationAddress();
        if (dAddress == null || dAddress.isEmpty()) {
            dAddress = mapboxService.reverseGeocode(
                    trajetRequest.getDestinationLatitude(), trajetRequest.getDestinationLongitude());
        }
        trajetLocation.setDestinationAddress(dAddress);
        trajetLocation.setEncodedPolyline(encodedPolyline);

        trajetLocation.setTrajet(trajet);
        trajet.setTrajetLocation(trajetLocation);

        trajet.setClient((Client) client);
        trajet.setChauffeur(null);
        trajet.setDistanceKm(distanceKm);
        trajet.setDurationMinutes(durationMinutes);
        trajet.setPrice(price);
        trajet.setScheduledAt(trajetRequest.getScheduledAt());
        trajet.setStatus(initialStatus);
        trajet.setRequestedAt(LocalDateTime.now());
        trajet.setScheduledAt(trajetRequest.getScheduledAt());
        trajet.setPreferredDriverGender(trajetRequest.getPreferredDriverGender());
        trajet.setPreferredDriverId(trajetRequest.getPreferredDriverId());

        if (trajetRequest.getPaymentMethod() == PaiementType.ONLINE) {
            Wallet wallet = walletService.getOrCreateWallet(client.getId());
            if (wallet.getBalance() < trajet.getPrice()) {
                throw new RuntimeException("Insufficient funds");
            }
        }

        Trajet saved = trajetRepository.save(trajet);

        Paiement paiement = new Paiement();
        paiement.setTrajet(saved);
        paiement.setMontant(saved.getPrice());
        PaiementType method = trajetRequest.getPaymentMethod() != null ? trajetRequest.getPaymentMethod() : PaiementType.CASH;
        paiement.setType(method);
        paiement.setStatus(PaiementStatus.EN_ATTENTE);
        paiementRepository.save(paiement);

        saved.setPaiement(paiement);
        saved.setPaymentMethod(method);

        System.out.println("ðŸ” [DB SAVE] Ride ID: " + saved.getId() + " Status: " + saved.getStatus() + " ScheduledAt: " + saved.getScheduledAt());
        System.out.println("ðŸ” [DB SAVE] Server Local Time is: " + LocalDateTime.now());

        // If it's a far-future scheduled ride, we don't dispatch immediately
        if (saved.getStatus() == TripStatus.Scheduled) {
            System.out.println("ðŸ“… Ride is far in the future. Waiting for scheduler.");
            TrajetResponse response = trajetMapper.toDTO(saved);
            if (saved.getScheduledAt() != null && response.getScheduledAt() == null) {
                response.setScheduledAt(saved.getScheduledAt());
            }
            return response;
        }

        System.out.println("📡 Dispatching ride to found drivers...");
        sendTrajetToDrivers(saved, drivers);

        // ONLY trigger the 15-second timeout for IMMEDIATE rides.
        // Scheduled rides (even if dispatched early) should stay alive until their departure time.
        if (saved.getScheduledAt() == null) {
            timeoutService.handleTimeout(saved.getId());
        } else {
            System.out.println("â³ Scheduled ride detected. Persistent until departure time (no 15s timeout).");
        }

        return trajetMapper.toDTO(saved);
    }

    public TrajetPreviewResponse previewTrajet(TrajetPreviewRequest request) {
        // --- Road Data from Mapbox ---
        MapboxService.RouteDetails roadmap = mapboxService.getRouteDetails(
                request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDestinationLatitude(), request.getDestinationLongitude()
        );

        // --- Address Resolution ---
        String pAddress = mapboxService.reverseGeocode(
                request.getPickupLatitude(), request.getPickupLongitude());

        String dAddress = mapboxService.reverseGeocode(
                request.getDestinationLatitude(), request.getDestinationLongitude());

        Double price = calculatePrice(roadmap.getDistanceKm());

        TrajetPreviewResponse response = new TrajetPreviewResponse();
        response.setDistanceKm(roadmap.getDistanceKm());
        response.setDurationMinutes(roadmap.getDurationMinutes());
        response.setPrice(price);
        response.setEncodedPolyline(roadmap.getEncodedPolyline());
        response.setPickupAddress(pAddress);
        response.setDestinationAddress(dAddress);

        return response;
    }

    private Double calculatePrice(Double distanceKm) {
        final double BASE_FARE = 0.7;
        final double PER_KM_RATE = 0.8;
        return BASE_FARE + (distanceKm * PER_KM_RATE);
    }

    public void sendTrajetToDrivers(Trajet trajet, List<Chauffeur> drivers) {
        TrajetResponse dto = trajetMapper.toDTO(trajet);
        // Force-set scheduledAt in case MapStruct's generated code is stale
        if (trajet.getScheduledAt() != null && dto.getScheduledAt() == null) {
            dto.setScheduledAt(trajet.getScheduledAt());
            System.out.println("âš ï¸ [BROADCAST] MapStruct missed scheduledAt â€” manually set it!");
        }
        System.out.println("ðŸ“¡ [BROADCAST] Preparing to send Trajet " + trajet.getId() + " to " + drivers.size() + " drivers.");
        System.out.println("   -> scheduledAt on Entity: " + trajet.getScheduledAt());
        System.out.println("   -> scheduledAt on DTO: " + dto.getScheduledAt());
        System.out.println("   -> Status: " + dto.getStatus());

        for (Chauffeur driver : drivers) {
            String destination = "/topic/driver/" + driver.getId();
            try {
                messagingTemplate.convertAndSend(destination, dto);
                System.out.println("âœ… [BROADCAST] Message sent successfully to " + destination);
            } catch (Exception e) {
                System.err.println("âŒ [BROADCAST] Error sending to " + destination + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    public List<Chauffeur> findDriversWithExpansion(double lat, double lon, String preferredGender) {
        Set<Chauffeur> driversSet = new LinkedHashSet<>();
        for (int i = 1; i < 10; i += 2) {
            List<Chauffeur> drivers;
            if (preferredGender != null && !preferredGender.isEmpty()) {
                drivers = chauffeurRepository.findNearbyDriversByGender(lat, lon, i, preferredGender);
            } else {
                drivers = chauffeurRepository.findNearbyDrivers(lat, lon, i);
            }
            driversSet.addAll(drivers);
            if (driversSet.size() >= 3) {
                return driversSet.stream().limit(3).toList();
            }
        }
        return new ArrayList<>(driversSet);
    }

    @Transactional
    public synchronized void handleDriverResponse(Long trajetId, String action) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Chauffeur chauffeur = (Chauffeur) userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chauffeur not found"));

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("Trajet not found"));

        if (trajet.getStatus() != TripStatus.Created) {
            throw new RuntimeException("Trajet dÃ©jÃ  pris");
        }

        if (action.equalsIgnoreCase("ACCEPT")) {
            trajet.setChauffeur(chauffeur);
            trajet.setStatus(TripStatus.Accepted);
            trajetRepository.save(trajet);

            messagingTemplate.convertAndSend(
                    "/topic/client/" + trajet.getClient().getId(),
                    buildDriverDetailsMap(chauffeur, trajet.getScheduledAt())
            );

            messagingTemplate.convertAndSend(
                    "/topic/trajet/" + trajetId,
                    Map.of("status", "TAKEN", "trajetId", trajetId)
            );
        }
    }

    private Map<String, Object> buildDriverDetailsMap(Chauffeur chauffeur, LocalDateTime scheduledAt) {
        Map<String, Object> driverDetails = new HashMap<>();
        driverDetails.put("status", "ACCEPTED");
        driverDetails.put("driverId", chauffeur.getId());
        driverDetails.put("driverName", chauffeur.getFullName());
        driverDetails.put("driverPhoto", chauffeur.getPhotoBase64() != null ? chauffeur.getPhotoBase64() : "");
        driverDetails.put("driverRating", chauffeur.getNoteMoyenne() != null ? chauffeur.getNoteMoyenne() : 0.0);
        driverDetails.put("vehicleModel", chauffeur.getVehicleModel() != null ? chauffeur.getVehicleModel() : "Moto-Taxi");
        driverDetails.put("vehiclePlate", chauffeur.getVehiclePlate() != null ? chauffeur.getVehiclePlate() : "");
        if (scheduledAt != null) {
            driverDetails.put("scheduledAt", scheduledAt.toString());
        }
        return driverDetails;
    }

    @Transactional
    public TrajetResponse startTrajet(Long trajetId) {
        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("trajet not found"));

        if (trajet.getStatus() != TripStatus.Accepted && trajet.getStatus() != TripStatus.Arrived) {
            throw new RuntimeException("Trajet dÃ©jÃ  pris ou non disponible (status=" + trajet.getStatus() + ")");
        }

        // Process payment if it's ONLINE
        paiementService.processTripPayment(trajetId);

        trajet.setStartedAt(LocalDateTime.now());
        trajet.setStatus(TripStatus.Started);
        Trajet saved = trajetRepository.save(trajet);

        messagingTemplate.convertAndSend("/topic/client/" + trajet.getClient().getId(),
                Map.of("status", "STARTED", "trajetId", trajetId, "message", "Trip started!"));
        messagingTemplate.convertAndSend("/topic/trajet/" + trajetId,
                Map.of("status", "STARTED", "trajetId", trajetId));

        return trajetMapper.toDTO(saved);
    }

    @Transactional
    public TrajetResponse terminerTrajet(Long trajetId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("chauffeur not found"));

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("trajet not found"));

        if (trajet.getChauffeur() == null || !trajet.getChauffeur().getId().equals(user.getId())) {
            throw new RuntimeException("Ce trajet ne t'appartient pas");
        }

        if (trajet.getStatus() != TripStatus.Started) {
            throw new RuntimeException("Le trajet n'est pas en cours");
        }
        trajet.setStatus(TripStatus.Completed);
        trajet.setCompletedAt(LocalDateTime.now());
        Trajet saved = trajetRepository.save(trajet);

        // Payment is now processed at startTrajet only, as per user requirement

        messagingTemplate.convertAndSend("/topic/client/" + trajet.getClient().getId(),
                Map.of("status", "COMPLETED", "trajetId", trajetId, "message", "Trip completed!"));
        messagingTemplate.convertAndSend("/topic/trajet/" + trajetId,
                Map.of("status", "COMPLETED", "trajetId", trajetId));

        return trajetMapper.toDTO(saved);
    }

    @Transactional
    public TrajetResponse annulerTrajet(Long trajetId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("user not found"));

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("trajet not found"));

        boolean isClient = trajet.getClient().getId().equals(user.getId());
        boolean isChauffeur = trajet.getChauffeur() != null && trajet.getChauffeur().getId().equals(user.getId());

        if (!isClient && !isChauffeur) {
            throw new RuntimeException("Ce trajet ne t'appartient pas");
        }

        if (trajet.getStatus() == TripStatus.Completed) {
            throw new RuntimeException("Impossible d'annuler un trajet terminé");
        }
        trajet.setStatus(TripStatus.Canceled);
        String cancelledBy = isClient ? "CLIENT" : "CHAUFFEUR";
        trajet.setCancelledBy(cancelledBy);

        Trajet saved = trajetRepository.save(trajet);

        messagingTemplate.convertAndSend("/topic/trajet/" + trajetId,
                Map.of("status", "CANCELLED", "trajetId", trajetId, "cancelledBy", cancelledBy));
        messagingTemplate.convertAndSend("/topic/client/" + trajet.getClient().getId(),
                Map.of("status", "CANCELLED", "trajetId", trajetId, "cancelledBy", cancelledBy));

        return trajetMapper.toDTO(saved);
    }

    public TrajetResponse getTrajetById(Long id){
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        User user=userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Trajet trajet = trajetRepository.findById(id).orElseThrow(() -> new RuntimeException("trajet not found"));

        Boolean isClient=trajet.getClient().getId().equals(user.getId());
        Boolean isChauffeur=trajet.getChauffeur()!=null && trajet.getChauffeur().getId().equals(user.getId());
        if (!isChauffeur && !isClient){
            throw new RuntimeException("Access denied");
        }
        return trajetMapper.toDTO(trajet);
    }

    public List<TrajetResponse> getAvailableTrajetsForDriver() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Chauffeur chauffeur = chauffeurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chauffeur not found"));

        if (chauffeur.getAvailability() == Availability.FALSE) {
            return Collections.emptyList();
        }

        if (chauffeur.getLocation() == null) {
            return Collections.emptyList();
        }

        double lat = chauffeur.getLocation().getLatitude();
        double lon = chauffeur.getLocation().getLongitude();

        Set<Trajet> trajetsSet = new LinkedHashSet<>();

        for (int i = 1; i < 10; i += 2) {
            List<Trajet> found = trajetRepository.findNearbyAvailableTrajets(lat, lon, i, chauffeur.getId());
            trajetsSet.addAll(found);
            if (trajetsSet.size() >= 5) {
                break;
            }
        }
        return trajetsSet.stream()
                .limit(5)
                .map(trajet -> {
                    TrajetResponse dto = trajetMapper.toDTO(trajet);
                    // Force-set scheduledAt in case MapStruct's generated code is stale
                    if (trajet.getScheduledAt() != null && dto.getScheduledAt() == null) {
                        dto.setScheduledAt(trajet.getScheduledAt());
                    }
                    return dto;
                })
                .toList();
    }

    @Transactional
    public TrajetResponse driverArrivedAtPickup(Long trajetId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Chauffeur chauffeur = chauffeurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chauffeur not found"));

        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new RuntimeException("Trajet not found"));

        if (!trajet.getChauffeur().getId().equals(chauffeur.getId())) {
            throw new RuntimeException("Ce trajet ne t'appartient pas");
        }

        trajet.setStatus(TripStatus.Arrived);
        Trajet saved = trajetRepository.save(trajet);

        String destination = "/topic/client/" + trajet.getClient().getId();
        System.out.println("ðŸ“¡ Sending ARRIVED notification to: " + destination);
        messagingTemplate.convertAndSend(
                destination,
                Map.of("status", "ARRIVED", "trajetId", trajetId, "message", "Your driver is here")
        );
        messagingTemplate.convertAndSend("/topic/trajet/" + trajetId,
                Map.of("status", "ARRIVED", "trajetId", trajetId));

        return trajetMapper.toDTO(saved);
    }

    public List<TrajetResponse> getClientHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User client = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Client not found"));
        return trajetRepository.findByClientIdOrderByRequestedAtDesc(client.getId())
                .stream()
                .map(trajetMapper::toDTO)
                .toList();
    }

    public List<TrajetResponse> getDriverHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User driver = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Driver not found"));
        return trajetRepository.findByChauffeurIdOrderByRequestedAtDesc(driver.getId())
                .stream()
                .map(trajetMapper::toDTO)
                .toList();
    }

    @Transactional
    public Optional<TrajetResponse> getActiveTrajetForDriver() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User driver = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Driver not found"));
        List<Trajet> active = trajetRepository.findActiveTrajetByChauffeurId(driver.getId());
        if (active.isEmpty()) return Optional.empty();
        return Optional.of(trajetMapper.toDTO(active.get(0)));
    }

    @Transactional
    public Optional<TrajetResponse> getActiveTrajetForClient() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User client = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Client not found"));
        List<Trajet> active = trajetRepository.findActiveTrajetByClientId(client.getId());
        if (active.isEmpty()) return Optional.empty();
        return Optional.of(trajetMapper.toDTO(active.get(0)));
    }

    @Transactional
    @Scheduled(fixedRate = 5000) // Every 5 seconds (Optimized from 30s)
    public void checkUpcomingScheduledRides() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("â° [SCHEDULER] Running at: " + now);

        // --- Phase 1: Triggering Upcoming Rides (now + 10 mins) ---
        LocalDateTime limit = now.plusMinutes(10);
        List<Trajet> upcoming = trajetRepository.findUpcomingScheduledTrajets(limit);

        if (upcoming.isEmpty()) {
            System.out.println("ðŸ“ [SCHEDULER] No rides matching query (status='Scheduled' AND scheduledAt <= " + limit + ")");
            // Debug: Check if there are ANY scheduled rides at all
            List<Trajet> allScheduled = trajetRepository.findByStatus(TripStatus.Scheduled);
            if (!allScheduled.isEmpty()) {
                System.out.println("ðŸ“ [SCHEDULER DEBUG] Found " + allScheduled.size() + " total 'Scheduled' rides in DB:");
                for (Trajet s : allScheduled) {
                    System.out.println("   -> ID: " + s.getId() + " is for: " + s.getScheduledAt());
                }
            }
        } else {
            System.out.println("ðŸ“ [SCHEDULER] Found " + upcoming.size() + " rides to trigger.");
        }

        for (Trajet t : upcoming) {
            System.out.println("ðŸš€ [SCHEDULER] Triggering ride ID " + t.getId());

            double lat = 0, lon = 0;
            if (t.getTrajetLocation() != null) {
                lat = t.getTrajetLocation().getPickupLatitude();
                lon = t.getTrajetLocation().getPickupLongitude();
            }
            System.out.println("ðŸ“ [SCHEDULER] Pickup Location: " + lat + ", " + lon);

            t.setStatus(TripStatus.Created);
            trajetRepository.save(t);

            List<Chauffeur> drivers = findDriversWithExpansion(lat, lon, t.getPreferredDriverGender());
            System.out.println("ðŸš— [SCHEDULER] Found " + drivers.size() + " drivers for ride " + t.getId());

            if (!drivers.isEmpty()) {
                System.out.println("ðŸ“¡ [SCHEDULER] Dispatching to WS...");
                sendTrajetToDrivers(t, drivers);
            }
        }

        // --- Phase 2: Expiring Missed Rides (time has passed) ---
        List<Trajet> expired = trajetRepository.findExpiredScheduledTrajets(now);
        for (Trajet e : expired) {
            System.out.println("âš ï¸ [SCHEDULER] Expiring ride ID " + e.getId() + " (Time reached, no driver)");

            e.setStatus(TripStatus.Canceled);
            trajetRepository.save(e);

            // Notify Rider
            messagingTemplate.convertAndSend(
                    "/topic/client/" + e.getClient().getId(),
                    Map.of("status", "NO_DRIVER", "message", "Votre trajet planifiÃ© a Ã©tÃ© annulÃ© car aucun chauffeur n'est disponible.", "trajetId", e.getId())
            );

            // Notify Drivers to remove card
            messagingTemplate.convertAndSend(
                    "/topic/trajet/" + e.getId(),
                    Map.of("status", "TIMEOUT", "trajetId", e.getId())
            );
        }
    }

    // --- ADMIN METHODS ---

    public List<ChauffeurStatResponse> getAllChauffeurStats() {
        List<Chauffeur> chauffeurs = chauffeurRepository.findAll();
        return chauffeurs.stream().map(this::calculateChauffeurStats).collect(Collectors.toList());
    }

    public List<TrajetResponse> getTrajetsByChauffeurId(Long chauffeurId) {
        return trajetRepository.findByChauffeurIdOrderByRequestedAtDesc(chauffeurId)
                .stream()
                .map(trajetMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<com.example.taximotoapp_backend.Admin.dto.AdminTrajetDto> getAllTrajetsForAdmin() {
        return trajetRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "requestedAt"))
                .stream()
                .map(this::toAdminTrajetDto)
                .collect(Collectors.toList());
    }

    private boolean checkSuspicious(Trajet trajet) {
        if (trajet.getStartedAt() == null || trajet.getCompletedAt() == null) {
            return false;
        }
        long actualMinutes = java.time.Duration.between(trajet.getStartedAt(), trajet.getCompletedAt()).toMinutes();
        int expectedMinutes = trajet.getDurationMinutes() != null ? trajet.getDurationMinutes() : 0;
        double distance = trajet.getDistanceKm() != null ? trajet.getDistanceKm() : 0.0;

        // 1. Durée excessive (au moins 3x le temps théorique et plus de 15 minutes)
        if (expectedMinutes > 0 && actualMinutes > (expectedMinutes * 3.0) && actualMinutes > 15) {
            return true;
        }

        // 2. Vitesse anormale par rapport à la distance (moins de 4 km/h pour un trajet significatif de plus de 15 minutes)
        if (distance > 0.5 && actualMinutes > 15) {
            double speed = distance / (actualMinutes / 60.0);
            if (speed < 4.0) {
                return true;
            }
        }

        // 3. Trajet fictif (terminé en moins d'une minute pour plus de 0.5 km)
        if (actualMinutes < 1 && distance > 0.5) {
            return true;
        }

        return false;
    }

    private String getSuspiciousReason(Trajet trajet) {
        if (trajet.getStartedAt() == null || trajet.getCompletedAt() == null) {
            return null;
        }
        long actualMinutes = java.time.Duration.between(trajet.getStartedAt(), trajet.getCompletedAt()).toMinutes();
        int expectedMinutes = trajet.getDurationMinutes() != null ? trajet.getDurationMinutes() : 0;
        double distance = trajet.getDistanceKm() != null ? trajet.getDistanceKm() : 0.0;

        if (expectedMinutes > 0 && actualMinutes > (expectedMinutes * 3.0) && actualMinutes > 15) {
            return "Durée excessive (" + actualMinutes + " min vs " + expectedMinutes + " min estimées)";
        }

        if (distance > 0.5 && actualMinutes > 15) {
            double speed = distance / (actualMinutes / 60.0);
            if (speed < 4.0) {
                return "Vitesse trop faible (" + (Math.round(speed * 10.0) / 10.0) + " km/h)";
            }
        }

        if (actualMinutes < 1 && distance > 0.5) {
            return "Trajet instantané suspect (" + actualMinutes + " min pour " + (Math.round(distance * 10.0) / 10.0) + " km)";
        }

        return null;
    }

    public com.example.taximotoapp_backend.Admin.dto.AdminTrajetStatsDto getTrajetStatsForAdmin() {
        java.time.LocalDateTime startOfDay = java.time.LocalDateTime.of(java.time.LocalDate.now(), java.time.LocalTime.MIN);

        long total = trajetRepository.count();
        long todayTotal = trajetRepository.countByRequestedAtAfter(startOfDay);
        long completed = trajetRepository.countByStatus(TripStatus.Completed);
        long canceled = trajetRepository.countByStatus(TripStatus.Canceled);
        long accepted = trajetRepository.countByStatus(TripStatus.Accepted);
        long arrived = trajetRepository.countByStatus(TripStatus.Arrived);
        long started = trajetRepository.countByStatus(TripStatus.Started);
        long created = trajetRepository.countByStatus(TripStatus.Created);
        long scheduled = trajetRepository.countByStatus(TripStatus.Scheduled);

        long inProgress = accepted + arrived + started;
        long pending = created + scheduled;

        double cancelRate = total > 0 ? (canceled * 100.0 / total) : 0;

        Double totalRevenue = trajetRepository.sumTotalRevenue();
        Double revenueToday = trajetRepository.sumRevenueSince(startOfDay);
        Double avgPrice = trajetRepository.avgCompletedPrice();
        Double avgDistance = trajetRepository.avgCompletedDistance();

        long suspectCount = trajetRepository.findAll().stream()
                .filter(this::checkSuspicious)
                .count();

        return com.example.taximotoapp_backend.Admin.dto.AdminTrajetStatsDto.builder()
                .totalTrajets(total)
                .trajetsToday(todayTotal)
                .completed(completed)
                .canceled(canceled)
                .inProgress(inProgress)
                .pending(pending)
                .cancelRate(Math.round(cancelRate * 10.0) / 10.0)
                .totalRevenue(totalRevenue != null ? totalRevenue : 0)
                .revenueToday(revenueToday != null ? revenueToday : 0)
                .avgPrice(avgPrice != null ? Math.round(avgPrice * 100.0) / 100.0 : 0)
                .avgDistanceKm(avgDistance != null ? Math.round(avgDistance * 10.0) / 10.0 : 0)
                .suspectCount(suspectCount)
                .build();
    }

    private com.example.taximotoapp_backend.Admin.dto.AdminTrajetDto toAdminTrajetDto(Trajet trajet) {
        return com.example.taximotoapp_backend.Admin.dto.AdminTrajetDto.builder()
                .id(trajet.getId())
                .status(trajet.getStatus() != null ? trajet.getStatus().name() : null)
                .requestedAt(trajet.getRequestedAt())
                .scheduledAt(trajet.getScheduledAt())
                .startedAt(trajet.getStartedAt())
                .completedAt(trajet.getCompletedAt())
                .pickupAddress(trajet.getTrajetLocation() != null ? trajet.getTrajetLocation().getPickupAddress() : null)
                .destinationAddress(trajet.getTrajetLocation() != null ? trajet.getTrajetLocation().getDestinationAddress() : null)
                .distanceKm(trajet.getDistanceKm())
                .durationMinutes(trajet.getDurationMinutes())
                .price(trajet.getPrice())
                .paymentMethod(trajet.getPaymentMethod() != null ? trajet.getPaymentMethod().name() : null)
                .clientId(trajet.getClient() != null ? trajet.getClient().getId() : null)
                .clientName(trajet.getClient() != null ? trajet.getClient().getFullName() : null)
                .chauffeurId(trajet.getChauffeur() != null ? trajet.getChauffeur().getId() : null)
                .chauffeurName(trajet.getChauffeur() != null ? trajet.getChauffeur().getFullName() : null)
                .cancelledBy(trajet.getCancelledBy())
                .isSuspect(checkSuspicious(trajet))
                .suspicionReason(getSuspiciousReason(trajet))
                .build();
    }

    private ChauffeurStatResponse calculateChauffeurStats(Chauffeur chauffeur) {
        List<Trajet> trajets = trajetRepository.findByChauffeurIdOrderByRequestedAtDesc(chauffeur.getId());
        return trajetMapper.toChauffeurStatResponse(chauffeur, trajets);
    }
}

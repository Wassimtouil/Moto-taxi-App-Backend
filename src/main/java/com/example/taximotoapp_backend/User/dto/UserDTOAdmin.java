package com.example.taximotoapp_backend.User.dto;

import com.example.taximotoapp_backend.User.model.Chauffeur;
import com.example.taximotoapp_backend.User.model.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UserDTOAdmin extends UserDTO {
    private String photoBase64;
    private String drivingLicenceBase64;
    private String carteGriseBase64;
    public UserDTOAdmin(User user) {
        super(user);
        this.photoBase64 = user.getPhotoBase64();
        if (user instanceof Chauffeur) {
            Chauffeur c = (Chauffeur) user;
            this.drivingLicenceBase64 = c.getDrivingLicenceBase64();
            this.carteGriseBase64 = c.getCarteGriseBase64();
        }
    }
}

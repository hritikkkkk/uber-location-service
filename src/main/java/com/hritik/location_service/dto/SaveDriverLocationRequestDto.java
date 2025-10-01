package com.hritik.location_service.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveDriverLocationRequestDto {
    String driverId;
    Double latitude;
    Double longitude;
}
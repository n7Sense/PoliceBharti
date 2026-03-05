package com.nst.ufrs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShotputUpsertRequest {
    private Long applicationNo;
    private Float attempt1;
    private Float attempt2;
    private Float attempt3;
}


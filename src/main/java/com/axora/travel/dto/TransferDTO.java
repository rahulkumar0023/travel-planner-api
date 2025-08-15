package com.axora.travel.dto;

import java.math.BigDecimal;

public record TransferDTO(String from, String to, BigDecimal amount) {}


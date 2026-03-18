package com.wireshark.dto;

import java.time.Instant;

public record TimeBucketCount(Instant time, long count) {
}

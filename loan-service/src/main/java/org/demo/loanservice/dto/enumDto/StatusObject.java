package org.demo.loanservice.dto.enumDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusObject {
    YES(true),
    NO(false);
    private final boolean value;
}

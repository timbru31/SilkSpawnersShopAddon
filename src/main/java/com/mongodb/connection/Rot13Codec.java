package com.mongodb.connection;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

@SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
public class Rot13Codec {
    @Getter
    private String cipher = "%%__USER__%%";
}

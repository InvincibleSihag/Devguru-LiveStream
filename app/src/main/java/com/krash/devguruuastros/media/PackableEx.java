package com.krash.devguruuastros.media;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}

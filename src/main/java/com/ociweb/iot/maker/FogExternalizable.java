package com.ociweb.iot.maker;

import com.ociweb.pronghorn.pipe.BlobReader;
import com.ociweb.pronghorn.pipe.BlobWriter;

public interface FogExternalizable {

    int messageSize(); // Estimate worst size so the maker can reason channel message sizes.

    void writeExternal(BlobWriter out);

    void readExternal(BlobReader in);
}

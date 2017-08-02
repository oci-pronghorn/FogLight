package com.ociweb.iot.maker;

import com.ociweb.pronghorn.pipe.BlobReader;
import com.ociweb.pronghorn.pipe.BlobWriter;

public interface FogExternalizable {

    void writeExternal(BlobWriter out);

    void readExternal(BlobReader in);
}

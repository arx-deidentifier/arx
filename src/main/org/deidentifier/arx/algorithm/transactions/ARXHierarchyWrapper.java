package org.deidentifier.arx.algorithm.transactions;

import org.deidentifier.arx.AttributeType.Hierarchy;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ARXHierarchyWrapper {

    public static GenHierarchy convert(Hierarchy h) {
        Dict d = new Dict(h.getHierarchy());
        return new GenHierarchy(h.getHierarchy(), d);
    }


    public static void main(String[] args) throws IOException {
        Hierarchy h = Hierarchy.create(new File("A.csv"), Charset.defaultCharset(), ';');
        Dict d = new Dict(h.getHierarchy());
        System.out.println(convert(h).rep(d));
    }
}

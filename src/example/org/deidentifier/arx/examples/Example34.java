package org.deidentifier.arx.examples;
import java.io.IOException;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.risk.hipaa.SafeHarborValidator;
import org.deidentifier.arx.risk.hipaa.Warning;

public class Example34 {


    private static Data.DefaultData createDemoData(){
        final Data.DefaultData data = Data.create();
        data.add("first name", "age", "gender", "code", "birth", "email-address", "SSN", "Bank", "Vehicle", "URL", "IP", "phone");
        data.add("Max", "34", "male", "81667", "2008-09-02", "", "123-45-6789", "GR16 0110 1250 0000 0001 2300 695", "", "http://demodomain.com", "8.8.8.8", "+49 1234566");
        data.add("Max", "45", "female", "81675", "2008-09-02", "user@arx.org", "", "", "WDD 169 007-1J-236589", "", "2001:db8::1428:57ab", "");
        data.add("Max", "66", "male", "89375", "2008-09-02", "demo@email.com", "", "", "", "", "", "");
        data.add("Max", "70", "female", "81931", "2008-09-02", "", "", "", "", "", "", "");
        data.add("Max", "34", "female", "81931", "2008-09-02", "", "", "", "", "", "", "");
        data.add("Max", "90", "male", "81931", "2008-09-02", "", "", "", "", "", "", "");
        data.add("Max", "45", "male", "81931", "2008-09-02", "", "", "", "", "", "", "");
        return data;
    }

    private static void printWarnings(Warning[] warnings){
        if(warnings.length == 0)
            System.out.println("No warnings");
        else {
            for(Warning w : warnings)
                System.out.println(w.toString());
        }
    }
    
    
    public static void main(final String[] args) throws IOException{
        Data.DefaultData data = createDemoData();

        DataHandle handle = data.getHandle();


        Warning[] warnings = SafeHarborValidator.validate(handle);

        printWarnings(warnings);
    }
}

package org.deidentifier.arx.gui.view.impl.menu;


public class QueryTokenizer {
    
    private QueryTokenizerListener listener;
    
    public QueryTokenizer(QueryTokenizerListener listener){
        this.listener = listener;
    }
    
    public void tokenize(String query){

        int quote = -1;
        boolean first = true;
        char[] data = query.toCharArray();
        for (int i=0; i<data.length; i++){
            if (data[i]=='\\'){
             // Skip next
                i++; 
            } else if (data[i]=='"'){
                // Start quote
                if (quote == -1){
                    quote = i; 
                // End quote
                } else {
                    if (first) {
                        listener.field(quote, i-quote+1);
                    } else {
                        listener.value(quote, i-quote+1);
                    }
                    quote = -1;
                    first = !first;
                }
            } else if (quote == -1 && data[i]=='(') {
                listener.begin(i);
            } else if (quote == -1 && data[i]==')') {
                listener.end(i);
            } else if (quote == -1 && i<data.length-2 && data[i]=='a' && data[i+1]=='n' && data[i+2]=='d') {
                listener.and(i, 3);
                i+=2;
            } else if (quote == -1 && i<data.length-1 && data[i]=='o' && data[i+1]=='r') {
                listener.or(i, 2);
                i++;
            } else if ((quote == -1 && i<data.length-1 && data[i]=='<' && data[i+1]=='=')) {
                listener.leq(i, 2);
                i++;
            } else if ((quote == -1 && i<data.length-1 && data[i]=='>' && data[i+1]=='=')) {
                listener.geq(i, 2);
                i++;
            } else if (quote == -1 && data[i]=='=') {
                listener.equals(i);
            } else if (quote == -1 && data[i]=='<') {
                listener.less(i);
            } else if (quote == -1 && data[i]=='>') {
                listener.greater(i);
            } else if (quote == -1 && (data[i]!=' ' && data[i]!='\t' && data[i]!='\n')){
                listener.invalid(i);
            }
            
            if (i>=data.length) break;
        }
    }

    public static interface QueryTokenizerListener {

        void geq(int start, int length);
        void invalid(int start);
        void value(int start, int length);
        void field(int start, int length);
        void begin(int start);
        void end(int start);
        void and(int start, int length);
        void or(int start, int length);
        void less(int start);
        void greater(int start);
        void leq(int start, int length);
        void equals(int start);
    }
}

/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.deidentifier.arx.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSubset;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.EqualDistanceTCloseness;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;

/**
 * A simple command-line client
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * 
 */
public class CommandLineClient {

    public static enum LDiversityVariant {
        DISTINCT,
        ENTROPY,
        RECURSIVE;
    }

    public static enum Metric {
        DM,
        DMSTAR,
        ENTROPY,
        HEIGHT,
        NMENTROPY,
        PREC,
        AECS
    }

    public static enum AttributeType {
        QI,
        IS,
        SA,
        ID
    }

    public static enum TClosenessVariant {
        EMD_EQUAL,
        EMD_HIERARCHICAL;
    }

    private String           output;
    private char             separator;
    private Data             data;
    private ARXConfiguration config;

    /**
     * Lets do it!.
     * 
     * @param args the arguments
     */
    public static void main(final String[] args) {

        System.out.println("Java Version: " + System.getProperty("java.version"));
        final CommandLineClient cli = new CommandLineClient();

        final OptionParser parser = new OptionParser();

        // datasets
        final OptionSpec<String> input = parser.accepts("i", "CSV input dataset filename").withRequiredArg().ofType(String.class).required();
        final OptionSpec<String> output = parser.accepts("o", "CSV filename of the output").withRequiredArg().ofType(String.class).required();
        final OptionSpec<String> seperator = parser.accepts("sep", "seperator of the input CSV; if omitted ';' is assumed").withRequiredArg().ofType(String.class).defaultsTo(";");

        // data defintion
        final OptionSpec<String> attributeName = parser.accepts("aName", "name of the attribute").withRequiredArg().ofType(String.class);
        final OptionSpec<String> attributeType = parser.accepts("aType", "type of the attribute; possible values are " + Arrays.toString(AttributeType.values())).requiredIf(attributeName).withRequiredArg().ofType(String.class);
        final OptionSpec<String> attributeHierarchy = parser.accepts("aHierarchy", "hierarchy file of the attribute").requiredIf(attributeName).withRequiredArg().ofType(String.class);
        // final OptionSpec<String> attributeDataType = parser.accepts("aDType", "data type of the attribute; possible values are " + Arrays.toString(DataType.values())).withRequiredArg().ofType(String.class);

        // k_anonymity
        final OptionSpec<String> k_anonymity = parser.accepts("kAnonymity", "specify if k-anonymity should be employed").withOptionalArg().ofType(String.class);
        final OptionSpec<Integer> kValue = parser.accepts("kValue", "value of k").requiredIf(k_anonymity).withRequiredArg().ofType(Integer.class);

        // d-presence
        final OptionSpec<String> d_presence = parser.accepts("dPresence", "specify if d-presence should be employed").withOptionalArg().ofType(String.class);
        final OptionSpec<Double> dMin = parser.accepts("dMin", "value of dmin for d-presence").requiredIf(d_presence).withRequiredArg().ofType(Double.class);
        final OptionSpec<Double> dMax = parser.accepts("dMax", "value of dmax for d-presence").requiredIf(d_presence).withRequiredArg().ofType(Double.class);
        final OptionSpec<String> subset = parser.accepts("subset", "subset file").requiredIf(d_presence).withRequiredArg().ofType(String.class);

        // TODO: multiple sensitive attributes! possible to specify attributes, separated by comma --> hierarchical t-closeness - specify hierarchies by comma as well

        // ldiversity
        final OptionSpec<String> l_diversity = parser.accepts("lDiversity", "specify if l-diversity should be employed").withOptionalArg().ofType(String.class);
        final OptionSpec<String> lAttribute = parser.accepts("lAttribute", "specifies the name of the attribute for this l-diversity criterion").requiredIf(l_diversity).withRequiredArg().ofType(String.class);
        final OptionSpec<String> lVariant = parser.accepts("lVariant", "variant of l-diversity; possible values are " + Arrays.toString(LDiversityVariant.values())).requiredIf(l_diversity).withRequiredArg().ofType(String.class);
        final OptionSpec<Double> lValue = parser.accepts("lValue", "value of l").requiredIf(l_diversity).withRequiredArg().ofType(Double.class);
        final OptionSpec<Double> cValue = parser.accepts("cValue", "value of c").withRequiredArg().ofType(Double.class);

        // tcloseness
        final OptionSpec<String> t_closeness = parser.accepts("tCloseness", "specify if t-closeness should be employed").withOptionalArg().ofType(String.class);
        final OptionSpec<String> tAttribute = parser.accepts("tAttribute", "specifies the name of the attribute for this l-diversity criterion").requiredIf(t_closeness).withRequiredArg().ofType(String.class);
        final OptionSpec<String> tVariant = parser.accepts("tVariant", "variant of l-diversity; possible values are " + Arrays.toString(TClosenessVariant.values())).requiredIf(t_closeness).withRequiredArg().ofType(String.class);
        final OptionSpec<String> tHierarchy = parser.accepts("tHierarchy", "for hierarchical t-closeness a hierarchy must be specified").withRequiredArg().ofType(String.class);
        final OptionSpec<Double> tValue = parser.accepts("tValue", "value of t").requiredIf(t_closeness).withRequiredArg().ofType(Double.class);

        // supression
        final OptionSpec<Double> s = parser.accepts("s", "value of supression").withRequiredArg().ofType(Double.class).required();

        // metric
        final OptionSpec<String> metric = parser.accepts("m", "metric, possible values " + Arrays.toString(Metric.values())).withRequiredArg().ofType(String.class).required();

        // misc options
        final OptionSpec<String> practical = parser.accepts("pm", "if present, practical monotonicity is assumed").withOptionalArg();

        try {
            final OptionSet options = parser.parse(args);

            // data
            // TODO: UGLY hack - how to get JOPT to accept Character as value type?
            cli.separator = options.valueOf(seperator).charAt(0);
            cli.data = Data.create(options.valueOf(input), cli.separator);
            cli.output = options.valueOf(output);

            // define attributes
            List<String> attributeNames = options.valuesOf(attributeName);

            List<AttributeType> attributeTypes = new ArrayList<AttributeType>();
            List<String> attributeTypeList = options.valuesOf(attributeType);
            for (int i = 0; i < attributeTypeList.size(); i++) {
                attributeTypes.add(AttributeType.valueOf(attributeTypeList.get(i).trim().toUpperCase()));
            }
            List<String> attributeHierarchies = options.valuesOf(attributeHierarchy);

            if (attributeHierarchies.size() != attributeTypes.size() || attributeHierarchies.size() != attributeNames.size()) {
                throw new IllegalArgumentException("each defined attribute has to have a defined type and hierarchy; in case of no hierarcy specifed define -aHierarhy NONE");
            }
            for (int i = 0; i < attributeNames.size(); i++) {

                switch (attributeTypes.get(i)) {
                case ID:
                    cli.data.getDefinition().setAttributeType(attributeNames.get(i), org.deidentifier.arx.AttributeType.IDENTIFYING_ATTRIBUTE);
                    break;
                case SA:
                    cli.data.getDefinition().setAttributeType(attributeNames.get(i), org.deidentifier.arx.AttributeType.SENSITIVE_ATTRIBUTE);
                    break;
                case IS:
                    cli.data.getDefinition().setAttributeType(attributeNames.get(i), org.deidentifier.arx.AttributeType.INSENSITIVE_ATTRIBUTE);
                    break;
                case QI:
                    if (attributeHierarchies.get(i).equalsIgnoreCase("none")) {
                        throw new IllegalArgumentException("quasi identifiers must have a hierarchy specified");
                    }
                    cli.data.getDefinition().setAttributeType(attributeNames.get(i), Hierarchy.create(attributeHierarchies.get(i), cli.separator));
                    break;

                default:
                    break;
                }

            }

            // create config
            cli.config = ARXConfiguration.create();
            cli.config.setMaxOutliers(options.valueOf(s));

            if (options.has(practical)) {
                cli.config.setPracticalMonotonicity(true);
            } else {
                cli.config.setPracticalMonotonicity(false);
            }

            // set metric
            Metric m = Metric.valueOf(options.valueOf(metric).trim().toUpperCase());
            org.deidentifier.arx.metric.Metric<?> metricInstance = null;
            switch (m) {
            case PREC:
                metricInstance = org.deidentifier.arx.metric.Metric.createPrecisionMetric();
                break;
            case HEIGHT:
                metricInstance = org.deidentifier.arx.metric.Metric.createHeightMetric();
                break;
            case DMSTAR:
                metricInstance = org.deidentifier.arx.metric.Metric.createDMStarMetric();
                break;
            case DM:
                metricInstance = org.deidentifier.arx.metric.Metric.createDMMetric();
                break;
            case ENTROPY:
                metricInstance = org.deidentifier.arx.metric.Metric.createEntropyMetric();
                break;
            case NMENTROPY:
                metricInstance = org.deidentifier.arx.metric.Metric.createNMEntropyMetric();
                break;
            case AECS:
                metricInstance = org.deidentifier.arx.metric.Metric.createAECSMetric();
                break;
            default:
                break;
            }
            cli.config.setMetric(metricInstance);

            // criteria
            // k-anonymity
            if (options.has(k_anonymity)) {
                cli.config.addCriterion(new KAnonymity(options.valueOf(kValue)));
            }

            // d-presence
            if (options.has(d_presence)) {
                final DataSubset subsetInstance = DataSubset.create(cli.data, Data.create(options.valueOf(subset), cli.separator));
                cli.config.addCriterion(new DPresence(options.valueOf(dMin), options.valueOf(dMax), subsetInstance));
                throw new IllegalArgumentException("currently not supported");
            }

            // l-diversity
            if (options.has(l_diversity)) {
                switch (LDiversityVariant.valueOf(options.valueOf(lVariant).trim().toUpperCase())) {
                case DISTINCT:
                    cli.config.addCriterion(new DistinctLDiversity(options.valueOf(lAttribute), options.valueOf(lValue).intValue()));
                    break;
                case ENTROPY:
                    cli.config.addCriterion(new EntropyLDiversity(options.valueOf(lAttribute), options.valueOf(lValue)));
                    break;
                case RECURSIVE:
                    if (!options.has(cValue)) {
                        throw new IllegalArgumentException("for recursive l-diversity a c value must be specified");
                    }
                    cli.config.addCriterion(new RecursiveCLDiversity(options.valueOf(lAttribute), options.valueOf(cValue), options.valueOf(lValue).intValue()));
                    break;
                default:
                    break;
                }
            }

            // t-closeness
            if (options.has(t_closeness)) {
                switch (TClosenessVariant.valueOf(options.valueOf(tVariant).trim().toUpperCase())) {
                case EMD_EQUAL:
                    cli.config.addCriterion(new EqualDistanceTCloseness(options.valueOf(tAttribute), options.valueOf(tValue)));
                    break;
                case EMD_HIERARCHICAL:
                    if (!options.has(tHierarchy)) {
                        throw new IllegalArgumentException("for hierarchical t-closeness a hierarchy must be specified");
                    }
                    cli.config.addCriterion(new HierarchicalDistanceTCloseness(options.valueOf(tAttribute), options.valueOf(tValue), Hierarchy.create(options.valueOf(tHierarchy), cli.separator)));
                    break;
                default:
                    break;
                }

            }

            System.out.println(cli.config);

        } catch (final Exception e) {
            try {
                System.err.println(e.getLocalizedMessage());
                parser.printHelpOn(System.out);
                System.exit(0);
            } catch (final IOException e1) {
                e1.printStackTrace();
            }
        }

        try {
            cli.run();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                parser.printHelpOn(System.out);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        System.out.println("DONE");
    }

    /**
     * Run.
     */
    private void run() throws IOException {
        // Create an instance of the anonymizer
        final ARXAnonymizer anonymizer = new ARXAnonymizer();
        final ARXResult result = anonymizer.anonymize(data, config);
        result.getOutput().save(output, separator);
    }
}

ARX - Open Source Data Anonymization Software
====

Note
------

ARX is the result of a research project. To support our research, please cite one of our papers instead of referencing our website in scientific articles. You can find an overview of papers about ARX [here](https://arx.deidentifier.org/publications/). If you are not sure which paper to cite, we recommend this one:

Prasser F., Eicher J., Spengler H., Bild R., Kuhn K. A. (2020) Flexible Data Anonymization Using ARX — Current Status and Challenges Ahead. Software Pract Exper 2020;1–28. [(Link)](https://doi.org/10.1002/spe.2812)

Thanks!

Introduction
------

ARX is a comprehensive open source software for anonymizing sensitive personal data. 
It has been designed from the ground up to provide high scalability, ease of use and a 
tight integration of the many different aspects relevant to data anonymization. Its highlights include:

 * Utility-focused anonymization using different statistical models
 * Syntactic privacy models, such as k-anonymity, ℓ-diversity, t-closeness and δ-presence
 * Semantic privacy models, such as (ɛ, δ)-differential privacy
 * Methods for optimizing the profitability of data publishing based on monetary cost-benefit analyses
 * Data transformation with generalization, suppression, microaggregation and top/bottom coding as well as global and local recoding
 * Methods for analyzing data utility
 * Methods for analyzing re-identification risks

The software is able to handle very large datasets on commodity hardware and features an intuitive cross-platform 
graphical user interface. You can find further information on the project [website](https://arx.deidentifier.org/). 

Development setup
------

Currently, the main development of ARX is carried out using Eclipse as an IDE and Ant as a build tool. Support for further IDEs such as IntelliJ IDEA and Maven is experimental.

The Ant build script features various targets that can be used to build different versions of ARX (e.g. including GUI code or not). To build only the core code using Maven, set the system property `core` to `true`. This will build a platform independent jar with the ARX main code module and no GUI components:

```$ mvn compile -Dcore=true``` 

Contributing and code of conduct
------

See [here](https://github.com/arx-deidentifier/arx/blob/master/contributing.md) and [here](https://github.com/arx-deidentifier/arx/blob/master/code_of_conduct.md).

License
------

ARX (C) 2012 - 2023 Fabian Prasser and Contributors.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, 
software distributed under the License is distributed on 
an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
either express or implied. See the License for the specific language 
governing permissions and limitations under the License. 

External Libraries
------

ARX uses external libraries. Their licenses are listed in the respective folders.

ARX - Open Source Data Anonymization Software
====

ARX is a comprehensive open source software for de-identifying sensitive personal data. 
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
graphical user interface. You can find further information on the project [website](http://arx.deidentifier.org/). 

License
------

ARX (C) 2012 - 2018 Fabian Prasser, Florian Kohlmayer and Contributors.

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

Development setup
------

Currently, the main development of ARX is carried out using Eclipse as an IDE and Ant as a build tool. Support for IntelliJ IDEA and Maven is experimental.

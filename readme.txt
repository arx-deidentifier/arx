INTRODUCTION
************

This project provides a generic Java framework for the efficient implementation of globally optimal full-domain anonymity algorithms. It implements several optimizations which require monotonic generalization hierarchies and monotonic metrics for information loss. It further provides an implementation of the Flash algorithm. Flash is a highly efficient algorithm that implements a novel strategy and fully exploits the implementation framework. It offers stable execution times.

The framework currently distinguishes between four different types of attributes:

- Insensitive attributes will be kept as is.
- Directly identifying attributes (such as name) will be removed from the dataset.
- Quasi-identifying attributes (such as age or zipcode) will be transformed by applying the provided generalization hierarchies.
- Sensitive attributes will be kept as is and can be utilized to generate l-diverse/t-close transformations. 

The supported metrics for information loss are: 

- Height.
- Precision.
- Monotonic Discernability (DM*).
- Non-Uniform Entropy. 

LICENSE
*******

The Flash framework is copyright (C) 2012 Florian Kohlmayer and Fabian Prasser. It is licensed under the GNU GPL3:

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

EXTERNAL LIBRARIES
******************

The framework uses external libraries. The according licenses are listed in license.txt

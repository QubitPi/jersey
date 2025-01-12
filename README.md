[//]: # " Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved. "
[//]: # "  "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Public License v. 2.0, which is available at "
[//]: # " http://www.eclipse.org/legal/epl-2.0. "
[//]: # "  "
[//]: # " This Source Code may also be made available under the following Secondary "
[//]: # " Licenses when the conditions for such availability set forth in the "
[//]: # " Eclipse Public License v. 2.0 are satisfied: GNU General Public License, "
[//]: # " version 2 with the GNU Classpath Exception, which is available at "
[//]: # " https://www.gnu.org/software/classpath/license.html. "
[//]: # "  "
[//]: # " SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 "

Deploying [jersey (fork)](https://github.com/QubitPi/jersey) to GitHub Pages
============================================================================

Building the documentation site involves **docs** sub-module only in this Maven project:

```bash
cd docs
mvn package -Dmaven.test.skip
```

(The commands above have been tested with OpenJDK 11)

A gh-pages deployable will be generated at **docs/target/docbook/index**. This fork publishes the generated site onto 
[gh-pages branch under root directory](https://github.com/QubitPi/jersey/tree/gh-pages)

Syncing with [upstream](https://github.com/eclipse-ee4j/jersey)
---------------------------------------------------------------

This fork has two branches

1. *gh-pages* branch that exclusively host the generated static site
2. *origin/master* branch which is the default branch of this fork. The syncing we are talking about here happens
   between this branch and upstream/master

We use a rebase-style sync:

```bash
git checkout master
git fetch upstream
git rebase upstream/master
git push origin master -f
```

---

[![Build Status](https://travis-ci.org/eclipse-ee4j/jersey.svg?branch=master)](https://travis-ci.org/eclipse-ee4j/jersey)
&nbsp;[![EPL-2.0](./etc/epl.svg)](https://www.eclipse.org/legal/epl-2.0/)
&nbsp;[![GPL+CPE-2.0](./etc/gpl.svg)](https://www.gnu.org/software/classpath/license.html)

### About Jersey

Jersey is a REST framework that provides JAX-RS Reference Implementation and more.
Jersey provides its own [APIs][jersey-api] that extend the JAX-RS toolkit with
additional features and utilities to further simplify RESTful service and client
development. Jersey also exposes numerous extension SPIs so that developers may
extend Jersey to best suit their needs.

Goals of Jersey project can be summarized in the following points:

*   Track the JAX-RS API and provide regular releases of production quality
    Reference Implementations that ships with GlassFish;
*   Provide APIs to extend Jersey & Build a community of users and developers;
    and finally
*   Make it easy to build RESTful Web services utilising Java and the
    Java Virtual Machine.

### Licensing and Governance
Jersey is licensed under a dual license - [EPL 2.0 and GPL 2.0 with Class-path Exception](LICENSE.md).
That means you can choose which one of the two suits your needs better and use it under those terms.

We use [contribution policy](CONTRIBUTING.md), which means we can only accept contributions under
 the terms of [ECA][eca].

### More Information on Jersey
See the [Jersey website][jersey-web] to access Jersey documentation. If you run into any issues or have questions,
ask at [jersey-dev@eclipse.org][jersey-users] (need to [subscribe][jersey-users-subscribe] first),
[StackOverflow][jersey-so] or file an issue on [Jersey GitHub Project][jersey-issues].
You can follow us on [Twitter][jersey-twitter], too.

[eca]: http://www.eclipse.org/legal/ECA.php
[jersey-api]: https://jersey.github.io/apidocs/latest/jersey/index.html
[jersey-issues]: https://github.com/eclipse-ee4j/jersey/issues
[jersey-so]: http://stackoverflow.com/questions/tagged/jersey
[jersey-users]: mailto:jersey-dev@eclipse.org
[jersey-users-subscribe]: https://accounts.eclipse.org/mailing-list/jersey-dev
[jersey-web]: https://projects.eclipse.org/projects/ee4j.jersey
[jersey-twitter]: https://twitter.com/gf_jersey

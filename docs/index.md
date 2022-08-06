# terrapin

The terrapin project provides the primary data owners the ability to let third
parties access their data safely. Primary owners can revoke access at any time,
removing the ability for a third party to access the data. During this time, the
data itself is store at the third party, encrypted via terrapin.

Under the covers, terrapin is a collections
of [independent projects](./subprojects.md)
and micro-services that can serve other purposes beyond the primary purpose of
the terrapin project. These subprojects as well as the primary project are all
licensed under the permissive Apache 2 license scheme.

As of right now, the software exists in a mono-repository to ease the
development process. Changes to the subprojects must not break the other
projects that rely on them. Using a mono-repository forces execution of all the
tests to ensure compatibility.


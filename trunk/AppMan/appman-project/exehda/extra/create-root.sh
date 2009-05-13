#!/bin/bash
# $1 is the dn of the administrator
# $2 if the suffix to use
#

ldapadd -x -W -D $1 <<EOF

dn: $2
objectClass: dcObject
objectClass: organization
o: $3
dc: $4

EOF


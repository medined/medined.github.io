#!/bin/bash

PASS=$(grep ^PASS 2020-07-11-kubebench-remediations.md | wc -l)
WARN=$(grep ^WARN 2020-07-11-kubebench-remediations.md | wc -l)
FAIL=$(grep ^FAIL 2020-07-11-kubebench-remediations.md | wc -l)
JUSTIFICATION=$(grep ^JUSTIFICATION 2020-07-11-kubebench-remediations.md | wc -l)
TOTAL=$(expr $PASS + $WARN + $FAIL + $JUSTIFICATION)

cat <<EOF
| Result        | Count |
| ------------- | ----: |
| PASS          | $PASS |
| FAIL          | $FAIL |
| WARN          | $WARN |
| JUSTIFICATION | $JUSTIFICATION |
| ------------- | ----: |
| Total         | $TOTAL |
EOF


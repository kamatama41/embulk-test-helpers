#!/usr/bin/env bash

set -eux

./gradlew removeRepository publish
git checkout -b snapshot
git push --force-with-lease origin snapshot

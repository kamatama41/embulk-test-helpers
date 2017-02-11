#!/usr/bin/env bash

set -eux

branch=${1}
./gradlew publish

git config --global user.email "shiketaudonko41@gmail.com"
git config --global user.name "kamatama41"
git add repository
git commit -m 'Release new version'
git checkout -b ${branch}
git push --force-with-lease origin ${branch}

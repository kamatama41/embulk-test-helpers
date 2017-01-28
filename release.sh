#!/usr/bin/env bash

set -eux

./gradlew uploadArchives

git config --global user.email "shiketaudonko41@gmail.com"
git config --global user.name "kamatama41"
git add repository
git commit -m 'release new version'
git checkout -b release
git push --force-with-lease origin release

set -e

sbt "+ core/test" > /dev/null
echo "Tests Passed"

sbt "+ core/doc" > /dev/null
echo "Docs Generated"

TEMP_DIR=$(mktemp -d)
echo "Creating GH-Pages Commit in $TEMP_DIR"

# Setup Home Page
header="---
title: \"\"
layout: home
---
"
# Header required to avoid page weirdness
echo "$header" > $TEMP_DIR/index.md
cat README.md >> $TEMP_DIR/index.md

# Copy other Jekyll Pages
cp -r pages/* $TEMP_DIR/

# Copy scala-docs
mkdir $TEMP_DIR/scala-2.13
cp -r core/target/scala-2.13/api $TEMP_DIR/scala-2.13
mkdir $TEMP_DIR/scala-2.12
cp -r core/target/scala-2.12/api $TEMP_DIR/scala-2.12

# Test, Confirm, and Push
pushd $TEMP_DIR > /dev/null
  echo "Launch Site Locally?"
  select yn in "Yes" "No"; do
    case $yn in
        Yes ) bundle exec jekyll serve; break;;
        No ) break;;
    esac
  done
  echo "Update Docs?"
  select yn in "Yes" "No"; do
    case $yn in
        Yes ) break;;
        No ) exit;;
    esac
  done
  git init -b gh-pages > /dev/null
  git add -A > /dev/null
  git commit -m 'deploy docs' > /dev/null
  git push -f git@github.com:jsbrucker/scala-result.git gh-pages
popd > /dev/null

echo "GH-Pages Updated"

set -e

sbt "+ core/test" > /dev/null
echo "Tests Passed"

sbt "+ core/doc" > /dev/null
echo "Docs Generated"

TEMP_DIR=$(mktemp -d)
echo "Creating GH-Pages Commit in $TEMP_DIR"
cp README.md $TEMP_DIR/index.md
cp pages/* $TEMP_DIR/
mkdir $TEMP_DIR/scala-2.13
cp -rf core/target/scala-2.13/api $TEMP_DIR/scala-2.13
mkdir $TEMP_DIR/scala-2.12
cp -rf core/target/scala-2.12/api $TEMP_DIR/scala-2.12

pushd $TEMP_DIR > /dev/null
  git init -b gh-pages > /dev/null
  git add -A > /dev/null
  git commit -m 'deploy docs' > /dev/null
  git push -f git@github.com:jsbrucker/scala-result.git gh-pages
popd > /dev/null

echo "GH-Pages Updated"

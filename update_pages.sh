sbt + doc

TEMP_DIR=$(mktemp -d)
echo $TEMP_DIR
cp README.md $TEMP_DIR/index.md
cp pages/* $TEMP_DIR/
mkdir $TEMP_DIR/scala-2.13
cp -rf target/jvm-2.13/api $TEMP_DIR/scala-2.13
mkdir $TEMP_DIR/scala-2.12
cp -rf target/jvm-2.12/api $TEMP_DIR/scala-2.12

pushd $TEMP_DIR
  git init -b gh-pages
  git add -A
  git commit -m 'deploy docs'
  git push -f git@github.com:jsbrucker/scala-result.git gh-pages
popd
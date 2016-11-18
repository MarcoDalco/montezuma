deps="$(find . -iname "build.gradle" | xargs grep 'compile ' | grep 'group' | sort | uniq)"

function printTemplate() {
cat << EOF
prebuilt_jar(
    name = '$1',
    binary_jar = '$2',
    visibility = ['PUBLIC'],
)
EOF
}

#remote_file(
#    name = '$1-file',
#    out = '$2',
#    url = 'http://central.maven.org/maven2/org/$4/$1/$3/$1-$3.jar',
#    sha1 = '0ec99fae8716984ec56489fb45d1ae752724bae7',
#    visibility = ['PUBLIC'],
#)

#prebuilt_jar(
#    name = '$1',
#    binary_jar = ':$1-file',
#)

while read i;
do
  name=$(echo $i | cut -d "'" -f4)
  group=$(echo $i | cut -d "'" -f2)
  version=$(echo $i | cut -d "'" -f6)
  classifier=$(echo $i | cut -d "'" -f8)
  file="$name-$version.jar"
  printTemplate "$name" "$file"
#echo http://central.maven.org/maven2/org/$group/$name/$version/$name-$version.jar
done <<<"$deps"

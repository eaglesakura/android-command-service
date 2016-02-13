#! /bin/sh

COMPILE_DIR="./src/test/protobuf"
TARGET_FILES="${COMPILE_DIR}/*.proto"
JAVAOUT_DIR="./src/test/java/"

for filepath in ${TARGET_FILES}; do
  echo "compile -> $filepath"
  protoc "--java_out=${JAVAOUT_DIR}" "--proto_path=${COMPILE_DIR}" "${filepath}"
done

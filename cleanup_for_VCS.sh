#!/bin/sh
src_path=montezuma-cases/src/generatedtests
previous_tests_path=montezuma-cases/previous_generated_tests
recordings_path=montezuma-cases/recordings

mvn clean
rm -fr $previous_tests_path/*
rm -fr $src_path/*
rm -fr $recordings_path/*

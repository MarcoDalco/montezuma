#!/bin/sh
src_path=montezuma-cases/src/generatedtests
previous_tests_path=montezuma-cases/previous_generated_tests

rm -fr $previous_tests_path/*
mv $src_path/* $previous_tests_path

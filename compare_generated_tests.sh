#!/bin/sh
src_path=montezuma-cases/src/generatedtests
previous_tests_path=montezuma-cases/previous_generated_tests

meld $src_path $previous_tests_path &
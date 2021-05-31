#!/usr/bin/env bash

gradle --quiet "installDist" && "./build/install/EBeanGen/bin/EBeanGen" "$@"
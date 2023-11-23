#!/bin/bash

eval 'find -maxdepth 2 -name res-dev | sed s/"[^/]*$"/update.xml/g | xargs -I {} ant -buildfile {}'
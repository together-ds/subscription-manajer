#!/usr/bin/env bash

#如果任何命令的退出状态不是0（表示成功），则立即退出脚本；
set -ex

cd manajer-ui
npm ci
npm install vite -g

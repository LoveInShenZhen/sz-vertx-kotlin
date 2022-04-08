#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os

from git import Repo, Remote, InvalidGitRepositoryError
from rich import print
from rich.console import Console

console = Console()

SOURCE_DIRS = [
    '/Users/kk/work/gm3/src/myquant.cn/platform',
    '/Users/kk/work/gm3/src/myquant.cn/goldminer3',
    '/Users/kk/work/gm3/src/myquant.cn/3rdparty',
    '/Users/kk/work/gm3/src/myquant.cn/dragonsunmoon'
]

DEFAULT_BRANCH = {
    'gmterm-serv': 'v5_master'
}


def has_upstream_remote(repo: Repo, remote_name: str = 'upstream') -> bool:
    """
    判断仓库是否具有名称为 upstream 的源
    :param remote_name:
    :param repo:
    :return:
    """
    remote: Remote
    for remote in repo.remotes:
        if remote.name == remote_name:
            return True

    return False


def fetch_and_pull(path: str):
    try:
        repo = Repo(path)
        echo(f"更新目录源码: {path}")

        os.chdir(path)

        echo('git fetch --all', style = 'bold green')
        ret = os.system('git fetch --all')
        if ret != 0:
            echo('发生错误', style = 'bold red on white')

        echo('git remote -v', style = 'bold green')
        os.system('git remote -v')

        branch = DEFAULT_BRANCH.get(os.path.basename(path), 'master')
        cmd = f'git checkout {branch}'
        echo(cmd, style = 'bold green')
        ret = os.system(cmd)
        if ret != 0:
            echo('发生错误', style = 'bold red on white')

        cmd = f'git pull origin {branch}'
        echo(cmd, style = 'bold green')
        ret = os.system(cmd)
        if ret != 0:
            echo('发生错误', style = 'bold red on white')

        if has_upstream_remote(repo):
            cmd = f'git pull upstream {branch}'
            echo(cmd, style = 'bold green')
            ret = os.system(cmd)
            if ret != 0:
                echo('发生错误', style = 'bold red on white')

        # remote: Remote
        # for remote in repo.remotes:
        #     echo(f"fetch from remote: {remote.name}")
        #     remote.fetch()

    except InvalidGitRepositoryError:
        echo(f"{path} 目录不是 git 仓库, 忽略", style = 'bold yellow')


def echo(msg: str, style = 'blue'):
    print(f"[yellow]==>[/yellow] [{style}]{msg}[/{style}]")


def main():
    for source_dir in SOURCE_DIRS:
        for sub_dir in os.listdir(source_dir):
            if sub_dir.startswith('.'):
                # 忽略以 . 开头的目录
                continue

            path = os.path.join(source_dir, sub_dir)
            fetch_and_pull(path)
            print('-' * 80)


if __name__ == "__main__":
    main()

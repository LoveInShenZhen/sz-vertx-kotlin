#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
from typing import List

import git.refs.head
from git import Repo, Remote, InvalidGitRepositoryError
from rich import print
from rich.console import Console
import subprocess

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


def run_cmd(args: List[str]):
    result = subprocess.run(args = args, stdout = subprocess.PIPE, stderr = subprocess.STDOUT)
    output_lines =[line.decode(encoding = 'UTF-8') for line in result.stdout.splitlines() ]
    has_error = False
    for line in output_lines:
        if line.find("error:") > -1:
            has_error = True
            break

    if result.returncode != 0:
        has_error = True

    if has_error:
        for line in output_lines:
            echo(line, style = 'bold red')
        exit(99)
    else:
        for line in output_lines:
            echo(line)


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
        run_cmd('git fetch --all'.split())

        echo('git remote -v', style = 'bold green')
        run_cmd('git remote -v'.split())

        branch = DEFAULT_BRANCH.get(os.path.basename(path), 'master')
        cmd = f'git checkout {branch}'
        echo(cmd, style = 'bold green')
        run_cmd(cmd.split())

        cmd = f'git pull origin {branch}'
        echo(cmd, style = 'bold green')
        run_cmd(cmd.split())

        if has_upstream_remote(repo):
            cmd = f'git pull upstream {branch}'
            echo(cmd, style = 'bold green')
            run_cmd(cmd.split())

        # remote: Remote
        # for remote in repo.remotes:
        #     echo(f"fetch from remote: {remote.name}")
        #     remote.fetch()

    except InvalidGitRepositoryError:
        echo(f"{path} 目录不是 git 仓库, 忽略", style = 'bold yellow')


def list_branches():
    repo_path = '/Users/kk/work/gm3/src/myquant.cn/dragonsunmoon/gmterm-serv'
    repo = Repo(repo_path)
    branch: git.refs.head.Head
    for branch in repo.branches:
        echo(branch.name)

def switch_to_branch(repo: Repo, branch_name:str):
    branch:git.refs.head.Head
    if repo.active_branch.name != branch_name:
        for branch in repo.branches:
            if branch.name == branch_name:
                if not repo.is_dirty():
                    branch.checkout()
                else:
                    echo("dirty")
                    repo.



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
    # main()
    repo_path = '/Users/kk/work/gm3/src/myquant.cn/dragonsunmoon/gmterm-serv'
    repo = Repo(repo_path)
    switch_to_branch(repo, 'master')

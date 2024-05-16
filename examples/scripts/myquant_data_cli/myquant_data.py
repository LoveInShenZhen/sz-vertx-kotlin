#!/usr/bin/env python
# -*- coding: utf-8 -*-
from __future__ import print_function, absolute_import

import datetime
import logging
import pathlib

import argh
from gm.api import *
import os

LOG_LEVEL = logging.DEBUG
logger = logging.getLogger(name = "app")

set_token('设置你的掘金用户token')


def init_logging():
    global LOG_LEVEL
    log_fmt = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    logging.basicConfig(level = LOG_LEVEL, format = log_fmt)


def default_csv_path(symbol: str) -> str:
    home = pathlib.Path.home()
    csv_file = f'{symbol}_instruments.csv'
    return os.path.join(home, csv_file)


@argh.arg('symbol', help = '掘金规范的证券代码')
@argh.arg('from_date', help = '起始日期 yyyy-mm-dd')
@argh.arg('csv_path', help = '保存的csv文件路径')
def history_symbol(symbol: str, from_date: str, csv_path: str):
    '''
    查询指定证券代码,从 from_date 到今天的所有历史码表数据, 并保存到指定文件
    '''
    end_date = datetime.date.today().strftime('%Y-%m-%d')
    df = get_history_symbol(symbol = symbol, start_date = from_date, end_date = datetime.date.today().strftime('%Y-%m-%d'), df = True)
    df.to_csv(csv_path)
    logger.info(f'{symbol} 从 {from_date} ~ {end_date} 的历史码表数据已保存到 {csv_path}')

    stk_get_adj_factor()


if __name__ == '__main__':
    init_logging()
    try:
        argh.dispatch_commands([history_symbol])
    except KeyboardInterrupt:
        logger.info('中断退出')

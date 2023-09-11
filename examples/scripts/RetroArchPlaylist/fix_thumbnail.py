#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import glob
import json
import os.path
import shutil
from typing import List

THUMBNAILS_DIR = '/home/deck/.var/app/org.libretro.RetroArch/config/retroarch/thumbnails'
PLAYLIST_DIR = '/home/deck/.var/app/org.libretro.RetroArch/config/retroarch/playlists'

# THUMBNAILS_DIR = '/Users/kk/Downloads/tmp/thumbnails'
# PLAYLIST_DIR = '/Users/kk/Downloads/tmp/json_playlist'


def load_lpl(fpath: str) -> List[dict]:
    with open(fpath, 'r') as f:
        json_txt = f.read()
    lpl_json = json.loads(json_txt)
    return lpl_json['items']


def fix_thumbnails(item: dict):
    print(f'fix for item:\n{json.dumps(item, indent = "    ", ensure_ascii = False)}')
    rom_path: str = item['path']
    rom_label: str = item['label']
    db_name: str = item['db_name']

    lpl_name, _ = os.path.splitext(db_name)
    rom_name, _ = os.path.splitext(os.path.basename(rom_path))

    for thumbnails_type in ['Named_Boxarts', 'Named_Snaps', 'Named_Titles']:
        dir_path = os.path.join(THUMBNAILS_DIR, lpl_name, thumbnails_type)
        fix_thumbnails_dir(dir_path, rom_name, rom_label)


def fix_thumbnails_dir(dir_path: str, rom_name: str, label: str):
    fname_list = os.listdir(dir_path)
    for fname in fname_list:
        name, ext_name = os.path.splitext(fname)
        if name == rom_name:
            new_name = fix_name(f'{label}{ext_name}')
            orinig_fpath = os.path.join(dir_path, fname)
            new_fpath = os.path.join(dir_path, new_name)
            print(f'{orinig_fpath} ==> {new_fpath}')
            shutil.move(orinig_fpath, new_fpath)


def fix_name(name: str) -> str:
    for c in '&*/:`<>?\|':
        name = name.replace(c, '_')
    return name


def fix_for_lpl(lpl_name: str):
    lpl_path = os.path.join(PLAYLIST_DIR, lpl_name)
    items = load_lpl(lpl_path)
    for it in items:
        fix_thumbnails(it)


if __name__ == '__main__':
    fix_for_lpl('FBA.lpl')
    fix_for_lpl('FC HACK.lpl')
    fix_for_lpl('GBA.lpl')
    fix_for_lpl('HACK.lpl')

    fix_for_lpl('JIEJI.lpl')
    fix_for_lpl('PCE CD.lpl')
    fix_for_lpl('PCE.lpl')
    fix_for_lpl('SFC.lpl')

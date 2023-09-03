#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json
import os.path
import shutil


def print_hi(name):
    # Use a breakpoint in the code line below to debug your script.
    print(f'Hi, {name}')  # Press ⇧⌘B to toggle the breakpoint.


def build_playlist(fpath: str, dist_dir: str):
    itme_list = []

    item = []
    with open(fpath, 'r') as f:
        for line in f.readlines():
            if line.startswith('{'):
                print(f'{fpath} 已经是json格式的 playlist, 无需转换')
                return

            if line.startswith('/ROM/'):
                line = line.replace('/ROM/', '/home/deck/emu_games/emu_roms/roms/')
            line = line.replace('[我干UFO制作合集 禁止转载.倒卖]', '')
            line = line.strip()

            if line == '':
                itme_list.append(item)
                item = []
            else:
                item.append(line)

    # /home/deck/emu_games/emu_roms/ROM/FC/FC 183.nes
    # FC 183
    # /retroarch/cores/nestopia_libretro_libnx.nro
    # 梦之企鵝物語
    # FC.lpl
    """
    {
      "path": "/home/deck/emu_games/emu_roms/roms/FC/FC 002.nes",
      "label": "FC 002",
      "core_path": "DETECT",
      "core_name": "DETECT",
      "crc32": "FC.lpl|serial",
      "db_name": "FC.lpl"
    },
    """
    entry_list = []
    for item in itme_list:
        if len(item) != 5:
            raise '格式错误'
        entry = {
            'path': item[0],
            'label': item[3],
            'core_path': 'DETECT',
            'core_name': 'DETECT',
            'crc32': f'{item[4]}|serial',
            'db_name': item[4]
        }
        entry_list.append(entry)

    result = {'items': entry_list}
    playlist_json = json.dumps(result, ensure_ascii = False, indent = '    ')
    # print(playlist_json)

    dest_fpath = os.path.join(dist_dir, os.path.basename(fpath))

    with open(dest_fpath, 'w') as f:
        f.write(playlist_json)
    print(f'{fpath} 转换完成')


# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    build_playlist('/Users/kk/Downloads/tmp/playlist/FBA.lpl', '/Users/kk/Downloads/tmp/json_playlist')
    build_playlist('/Users/kk/Downloads/tmp/playlist/FC HACK.lpl', '/Users/kk/Downloads/tmp/json_playlist')
    build_playlist('/Users/kk/Downloads/tmp/playlist/GBA.lpl', '/Users/kk/Downloads/tmp/json_playlist')
    build_playlist('/Users/kk/Downloads/tmp/playlist/HACK.lpl', '/Users/kk/Downloads/tmp/json_playlist')
    build_playlist('/Users/kk/Downloads/tmp/playlist/JIEJI.lpl', '/Users/kk/Downloads/tmp/json_playlist')
    build_playlist('/Users/kk/Downloads/tmp/playlist/PCE CD.lpl', '/Users/kk/Downloads/tmp/json_playlist')
    build_playlist('/Users/kk/Downloads/tmp/playlist/PCE.lpl', '/Users/kk/Downloads/tmp/json_playlist')
    build_playlist('/Users/kk/Downloads/tmp/playlist/SFC.lpl', '/Users/kk/Downloads/tmp/json_playlist')

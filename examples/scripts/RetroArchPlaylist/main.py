#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json
import os


def print_hi(name):
    # Use a breakpoint in the code line below to debug your script.
    print(f'Hi, {name}')  # Press ⇧⌘B to toggle the breakpoint.


def build_playlist(fpath: str):
    itmes = []

    it = []
    with open(fpath, 'r') as f:
        for line in f.readlines():
            li = line.strip()
            if li == '':
                itmes.append(it)
                it = []
            else:
                it.append(li)

    # /home/deck/emu_games/emu_roms/ROM/FC/FC 183.nes
    # FC 183
    # /retroarch/cores/nestopia_libretro_libnx.nro
    # 梦之企鵝物語
    # FC.lpl
    """
    {
      "path": "/home/deck/emu_games/emu_roms/ROM/FC/FC 002.nes",
      "label": "FC 002",
      "core_path": "DETECT",
      "core_name": "DETECT",
      "crc32": "FC.lpl|serial",
      "db_name": "FC.lpl"
    },
    """
    entry_list = []
    for it in itmes:
        if len(it) != 5:
            raise '格式错误'
        entry = {
            'path': it[0],
            'label': it[3],
            'core_path': 'DETECT',
            'core_name': 'core_name',
            'crc32': f'{it[4]}|serial',
            'db_name': it[4]
        }
        entry_list.append(entry)

    result = {'items': entry_list}
    playlist_json = json.dumps(result, ensure_ascii = False, indent = '    ')
    print(playlist_json)

    playlist_fpath = f'{fpath}.json'


# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    build_playlist('/Users/kk/Downloads/tmp/retroarch/playlists/FC.lpl')

# See PyCharm help at https://www.jetbrains.com/help/pycharm/

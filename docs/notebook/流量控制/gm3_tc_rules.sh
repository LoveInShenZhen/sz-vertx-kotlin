#!/usr/bin/env bash

export LANG="en_US.UTF-8"

COMMAND=$1

DEV='eth0'

function setColor()
{
        tput setaf $1 2>/dev/null
}

function resetColor()
{
        tput sgr0 2>/dev/null
}

function echoColor() {
    setColor 3
    echo $1
    resetColor
}

function setup_tc_rules() {
  # 带宽上限
  full_bandwith='1000mbps'
  # 历史行情 + 基础数据查询 分配的带宽上限
  history_bandwidth='40mbps'
  # 实时行情 分配的带宽上限
  real_bandwidth='40mbps'
  # 其他服务使用剩余带宽
  other_bandwidth='900mbps'

  echo '==> clean old gm3 traffic control rules'
  clean_tc_rules
  echo '==> setup new gm3 traffic control rules'
  
  tc qdisc add dev eth0 root handle 1: htb default 30
  tc class add dev eth0 parent 1: classid 1:1 htb rate ${full_bandwith} ceil ${full_bandwith}

  echo "==> 历史行情: 7036,7037 + 基础数据查询: 7041, 7042 分配 [${history_bandwidth}]"
  tc class add dev eth0 parent 1:1 classid 1:10 htb rate ${history_bandwidth} ceil ${history_bandwidth}

  echo "==> 实时行情: 7021, 7022  分配 [${real_bandwidth}]"
  tc class add dev eth0 parent 1:1 classid 1:20 htb rate ${real_bandwidth} ceil ${real_bandwidth}

  echo "==> 其他服务端口, 不做限制"
  tc class add dev eth0 parent 1:1 classid 1:30 htb rate ${other_bandwidth} ceil ${full_bandwith}

  tc qdisc add dev eth0 parent 1:10 handle 10: sfq perturb 10
  tc qdisc add dev eth0 parent 1:20 handle 20: sfq perturb 10
  tc qdisc add dev eth0 parent 1:30 handle 30: sfq perturb 10

  tc filter add dev eth0 protocol ip parent 1:0 prio 1 u32 match ip sport 7036 0xffff flowid 1:10
  tc filter add dev eth0 protocol ip parent 1:0 prio 1 u32 match ip sport 7037 0xffff flowid 1:10
  tc filter add dev eth0 protocol ip parent 1:0 prio 1 u32 match ip sport 7041 0xffff flowid 1:10
  tc filter add dev eth0 protocol ip parent 1:0 prio 1 u32 match ip sport 7042 0xffff flowid 1:10

  tc filter add dev eth0 protocol ip parent 1:0 prio 1 u32 match ip sport 7021 0xffff flowid 1:20
  tc filter add dev eth0 protocol ip parent 1:0 prio 1 u32 match ip sport 7022 0xffff flowid 1:20

  echo "==> 测试端口: 10086, 带宽限制与历史行情相同 [${history_bandwidth}], 用于验证流量控制是否有效"
  tc filter add dev eth0 protocol ip parent 1:0 prio 1 u32 match ip sport 10086 0xffff flowid 1:10 

  echoColor 'setup gm3 traffic control rules finished.    [OK]'
}

function clean_tc_rules() {
  htb_qdisc_count=`tc qdisc show dev eth0 | grep root | grep htb | wc -l`

  if [ $htb_qdisc_count -eq 0 ]
  then
    echoColor 'No gm3 tc rules are configured.'
    return
  else
    tc qdisc del dev ${DEV} root handle 1:
    echoColor 'Clean gm3 tc rules.    [OK]'
  fi
}

function status_tc_rules() {
  echoColor '-------- qdisc --------'
  tc qdisc ls dev ${DEV}
  echo ''
  
  echoColor '-------- class --------'
  tc class ls dev ${DEV}
  echo ''

  echoColor '-------- filter -------'
  tc filter ls dev ${DEV}
}


# Check input arguments
checkArgs() {
    # Check command
    case "$COMMAND" in
        (setup | clean | status) ;;
        *)
            echoColor "Unknown arguments"
            return 1
        ;;
    esac
}

# Check input arguments
checkArgs
if [ $? != 0 ]
then
    echoColor "Usage: $0 { setup | clean | status }"
    exit 1
fi

set -e

case "${COMMAND}" in

    setup)
        setup_tc_rules
    ;;

    clean)
        clean_tc_rules
    ;;

    status)
        status_tc_rules
    ;;

esac

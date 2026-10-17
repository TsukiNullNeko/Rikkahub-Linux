#!/usr/bin/env python3
"""Convert a base64 vless:// subscription into a mihomo (clash.meta) config.

Usage: sub2mihomo.py <subscription-file-or-url> <output-dir> [port]
"""
import base64
import json
import os
import sys
import urllib.parse as up
import urllib.request

PORT = 10810


def load_subscription(src: str) -> str:
    if src.startswith("http://") or src.startswith("https://"):
        req = urllib.request.Request(src, headers={"User-Agent": "clash-verge/2.0"})
        with urllib.request.urlopen(req, timeout=30) as resp:
            return resp.read().decode("utf-8", "replace")
    return open(src, "r", encoding="utf-8").read()


def parse_nodes(text: str) -> list:
    data = text.strip()
    if "vless://" not in data:
        padded = data + "=" * (-len(data) % 4)
        try:
            data = base64.b64decode(padded).decode("utf-8", "replace")
        except Exception:
            pass

    nodes = []
    for line in data.splitlines():
        line = line.strip()
        if not line.startswith("vless://"):
            continue
        try:
            body = line[len("vless://"):]
            frag = ""
            if "#" in body:
                body, frag = body.split("#", 1)
            name = up.unquote(frag) or ("node-%d" % (len(nodes) + 1))
            userinfo, hostport = body.split("@", 1)
            query = ""
            if "?" in hostport:
                hostport, query = hostport.split("?", 1)
            host, port = hostport.rsplit(":", 1)
            params = {k: (v[0] if v else "") for k, v in up.parse_qs(query).items()}

            net = params.get("type", "tcp")
            security = params.get("security", "none")
            sni = params.get("host", "") or params.get("sni", "")
            path = params.get("path", "/")
            fp = params.get("fp", "")
            pbk = params.get("pbk", "")
            sid = params.get("sid", "")

            proxy = {
                "name": name,
                "type": "vless",
                "server": host,
                "port": int(port),
                "uuid": userinfo,
                "udp": True,
                "tls": security in ("tls", "reality"),
            }
            if sni:
                proxy["servername"] = sni
            if fp:
                proxy["client-fingerprint"] = fp
            if net == "ws":
                proxy["network"] = "ws"
                ws = {"path": path or "/"}
                if sni:
                    ws["headers"] = {"Host": sni}
                proxy["ws-opts"] = ws
            elif net == "grpc":
                proxy["network"] = "grpc"
                proxy["grpc-opts"] = {"grpc-service-name": path}
            else:
                proxy["network"] = "tcp"
            if security == "reality" and pbk:
                proxy["reality-opts"] = {"public-key": pbk, "short-id": sid}
            nodes.append(proxy)
        except Exception as e:
            print("skip node: %s" % e, file=sys.stderr)
    return nodes


def build_config(nodes: list, port: int) -> dict:
    names = [n["name"] for n in nodes]
    return {
        "mixed-port": port,
        "allow-lan": False,
        "bind-address": "127.0.0.1",
        "mode": "rule",
        "log-level": "warning",
        "unified-delay": True,
        "tcp-concurrent": True,
        "dns": {
            "enable": True,
            "listen": "127.0.0.1:1053",
            "ipv6": False,
            "default-nameserver": ["223.5.5.5", "119.29.29.29", "114.114.114.114"],
            "nameserver": ["223.5.5.5", "119.29.29.29", "114.114.114.114"],
        },
        "proxies": nodes,
        "proxy-groups": [
            {
                "name": "PROXY",
                "type": "url-test",
                "proxies": names,
                "url": "http://www.gstatic.com/generate_204",
                "interval": 300,
                "tolerance": 80,
            }
        ],
        "rules": ["MATCH,PROXY"],
    }


def main():
    src = sys.argv[1]
    out_dir = sys.argv[2]
    global PORT
    if len(sys.argv) > 3:
        PORT = int(sys.argv[3])

    text = load_subscription(src)
    nodes = parse_nodes(text)
    if not nodes:
        print("no nodes parsed", file=sys.stderr)
        sys.exit(1)

    os.makedirs(out_dir, exist_ok=True)
    config = build_config(nodes, PORT)
    try:
        import yaml
        with open(os.path.join(out_dir, "config.yaml"), "w") as f:
            yaml.safe_dump(config, f, allow_unicode=True, sort_keys=False)
    except ImportError:
        with open(os.path.join(out_dir, "config.yaml"), "w") as f:
            json.dump(config, f)
    print("nodes: %d -> %s/config.yaml (port %d)" % (len(nodes), out_dir, PORT))


if __name__ == "__main__":
    main()

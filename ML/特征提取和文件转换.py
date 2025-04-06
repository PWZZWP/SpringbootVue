import csv
from scapy.all import rdpcap
from collections import defaultdict
import time


def extract_features(packets):
    result = []
    # 用于跟踪 TCP 流的状态
    tcp_flows = defaultdict(lambda: {'start_time': None, 'end_time': None, 'window_sizes': [], 'zero_window_count': 0,
                                     'response_packets': []})
    for packet in packets:
        feature = {
            'SrcIp': None,
            'DstIp': None,
            'SrcPort': None,
            'DstPort': None,
            'HttpOpen': None,
            'TcpWindowSize': None,
            'ZeroWindow': None,
            'Duration': None,
            'ResPacketSplit': None,
            'Prediction': None
        }
        if 'IP' in packet:
            feature['SrcIp'] = packet['IP'].src
            feature['DstIp'] = packet['IP'].dst
            if 'TCP' in packet:
                feature['SrcPort'] = packet['TCP'].sport
                feature['DstPort'] = packet['TCP'].dport
                flow_key = (feature['SrcIp'], feature['SrcPort'], feature['DstIp'], feature['DstPort'])
                if tcp_flows[flow_key]['start_time'] is None:
                    tcp_flows[flow_key]['start_time'] = packet.time
                tcp_flows[flow_key]['end_time'] = packet.time
                tcp_flows[flow_key]['window_sizes'].append(packet['TCP'].window)
                if packet['TCP'].window == 0:
                    tcp_flows[flow_key]['zero_window_count'] += 1
                feature['TcpWindowSize'] = packet['TCP'].window
                feature['ZeroWindow'] = 1 if packet['TCP'].window == 0 else 0
            if 'HTTP' in packet:
                feature['HttpOpen'] = 1
        result.append(feature)
    # 计算 Duration
    for flow_key, flow_info in tcp_flows.items():
        if flow_info['start_time'] and flow_info['end_time']:
            duration = flow_info['end_time'] - flow_info['start_time']
            for entry in result:
                if (entry['SrcIp'], entry['SrcPort'], entry['DstIp'], entry['DstPort']) == flow_key:
                    entry['Duration'] = duration
    return result


def pcapng_to_csv(pcapng_file, csv_file):
    try:
        packets = rdpcap(pcapng_file)
        features = extract_features(packets)
        with open(csv_file, 'w', newline='') as f:
            fieldnames = ['SrcIp', 'DstIp', 'SrcPort', 'DstPort', 'HttpOpen', 'TcpWindowSize', 'ZeroWindow', 'Duration',
                          'ResPacketSplit', 'Prediction']
            writer = csv.DictWriter(f, fieldnames=fieldnames)
            writer.writeheader()
            for feature in features:
                writer.writerow(feature)
    except Exception as e:
        print(f"An error occurred: {e}")


if __name__ == "__main__":
    pcapng_file = 'traffic.pcapng'
    csv_file = 'output.csv'
    pcapng_to_csv(pcapng_file, csv_file)
    
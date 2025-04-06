import random
import pandas as pd

# 生成单条数据
def generate_single_data():
    A = '是' if random.random() < 0.95 else '否'
    if A == '否':
        return [A, None, None, None, None, 'NORMAL']
    B = '是' if random.random() < 0.85 else '否'
    if B == '否':
        return [A, B, None, None, None, 'NORMAL']
    C = '是' if random.random() < 0.85 else '否'
    if C == '否':
        return [A, B, C, None, None, 'NORMAL']
    D = '是' if random.random() < 0.85 else '否'
    if D == '否':
        return [A, B, C, D, None, 'NORMAL']
    E = '是' if random.random() < 0.85 else '否'
    label = 'ABNORMAL' if E == '是' else 'NORMAL'
    return [A, B, C, D, E, label]

# 生成指定数量的正常和异常数据
def generate_data(num_normal, num_abnormal):
    data = []
    while len([d for d in data if d[5] == 'NORMAL']) < num_normal or len([d for d in data if d[5] == 'ABNORMAL']) < num_abnormal:
        single_data = generate_single_data()
        if single_data[5] == 'NORMAL' and len([d for d in data if d[5] == 'NORMAL']) < num_normal:
            data.append(single_data)
        elif single_data[5] == 'ABNORMAL' and len([d for d in data if d[5] == 'ABNORMAL']) < num_abnormal:
            data.append(single_data)
    return data

# 生成 100 条数据，正常和异常各 50 条
all_data = generate_data(500, 500)

# 转换为 DataFrame
df = pd.DataFrame(all_data, columns=['A', 'B', 'C', 'D', 'E', 'label'])

# 保存数据到 CSV 文件
df.to_csv('cart_dataset.csv', index=False)
    
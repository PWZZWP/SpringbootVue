import random
import pandas as pd
from sklearn.impute import SimpleImputer
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeClassifier
from sklearn.metrics import accuracy_score
from sklearn2pmml import PMMLPipeline, sklearn2pmml
from sklearn2pmml.decoration import CategoricalDomain
import matplotlib.pyplot as plt
from sklearn import tree

# 生成随机IP地址
def generate_ip():
    return '.'.join(map(str, (random.randint(0, 255) for _ in range(4))))

# 生成单条数据（强化httpOpen作为根特征）
def generate_single_data():
    # 新增特征
    srcIp = generate_ip()
    dstIp = generate_ip()
    srcPort = random.randint(1024, 65535)
    dstPort = random.choice([80, 8080, 443])

    # 强化httpOpen的区分度
    httpOpen = 'Yes' if random.random() < 0.5 else 'No'

    # 修改：当httpOpen等于No时，直接判断为normal
    if httpOpen == 'No':
        return [httpOpen, None, None, None, None, 'NORMAL', srcIp, dstIp, srcPort, dstPort]

    # 当httpOpen为Yes时在进行后面逻辑判断
    tcpWindowSize = random.randint(0, 2000)
    tcpWindowSizeFlag = 'Yes' if tcpWindowSize <= 1024 else 'No'
    if tcpWindowSizeFlag == 'No':
        return [httpOpen, tcpWindowSize, None, None, None, 'NORMAL', srcIp, dstIp, srcPort, dstPort]

    zeroWindow = random.randint(0, 20)
    zeroWindowFlag = 'Yes' if zeroWindow > 10 else 'No'
    if zeroWindowFlag == 'No':
        return [httpOpen, tcpWindowSize, zeroWindow, None, None, 'NORMAL', srcIp, dstIp, srcPort, dstPort]

    duration = random.randint(0, 600)
    durationFlag = 'Yes' if duration > 300 else 'No'
    if durationFlag == 'No':
        return [httpOpen, tcpWindowSize, zeroWindow, duration, None, 'NORMAL', srcIp, dstIp, srcPort, dstPort]

    resPacketSplit = random.randint(0, 2000)
    resPacketSplitFlag = 'Yes' if resPacketSplit > 1000 else 'No'
    label = 'ABNORMAL' if resPacketSplitFlag == 'Yes' else 'NORMAL'
    return [httpOpen, tcpWindowSize, zeroWindow, duration, resPacketSplit, label, srcIp, dstIp, srcPort, dstPort]

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

# 生成 1000 条数据，正常和异常各 500 条
all_data = generate_data(500, 500)

# 转换为 DataFrame
df = pd.DataFrame(all_data, columns=['httpOpen', 'tcpWindowSize', 'zeroWindow', 'duration', 'resPacketSplit', 'label', 'srcIp', 'dstIp', 'srcPort', 'dstPort'])

# 检查所有 httpOpen 为 'No' 的样本是否都被标记为 'NORMAL'
check_df = df[df['httpOpen'] == 'No']
if not all(check_df['label'] == 'NORMAL'):
    print("存在 httpOpen 为 'No' 但 label 不为 'NORMAL' 的样本")
else:
    print("所有 httpOpen 为 'No' 的样本都被正确标记为 'NORMAL'")

# 保存数据到 CSV 文件
df.to_csv('cart_dataset2.csv', index=False)

# 从 CSV 文件读取数据
df = pd.read_csv('cart_dataset2.csv')

# 缺失值处理（排除新特征）
imputer = SimpleImputer(strategy='most_frequent')
df[['httpOpen', 'tcpWindowSize', 'zeroWindow', 'duration', 'resPacketSplit']] = imputer.fit_transform(df[['httpOpen', 'tcpWindowSize', 'zeroWindow', 'duration', 'resPacketSplit']])

# 将数值型特征转换为Yes/No标志
df['tcpWindowSize<=1024'] = df['tcpWindowSize'].apply(lambda x: 'Yes' if x <= 1024 else 'No')
df['zeroWindow>10/min'] = df['zeroWindow'].apply(lambda x: 'Yes' if x > 10 else 'No')
df['duration>300s'] = df['duration'].apply(lambda x: 'Yes' if x > 300 else 'No')
df['resPacketSplit>1000p'] = df['resPacketSplit'].apply(lambda x: 'Yes' if x > 1000 else 'No')

# 特征编码（排除新特征）
le = LabelEncoder()
df['label'] = le.fit_transform(df['label'])
feature_encoders = {}
for col in ['httpOpen', 'tcpWindowSize<=1024', 'zeroWindow>10/min', 'duration>300s', 'resPacketSplit>1000p']:
    feature_encoders[col] = LabelEncoder()
    df[col] = feature_encoders[col].fit_transform(df[col])

# 划分训练集和测试集（排除新特征）
X = df[['httpOpen', 'tcpWindowSize<=1024', 'zeroWindow>10/min', 'duration>300s', 'resPacketSplit>1000p']]
y = df['label']
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# 构建 CART 决策树模型（强制httpOpen作为根特征）
model = DecisionTreeClassifier(
    criterion='gini',
    splitter='best',
    random_state=0,
    max_depth=4
)

# 继续使用所有特征进行后续分裂
model.fit(X_train, y_train)

# 模型预测
y_pred = model.predict(X_test)

# 评估模型
accuracy = accuracy_score(y_test, y_pred)
print(f"模型准确率: {accuracy}")
print(f"训练集准确率：{model.score(X_train, y_train) * 100}%")

# 示例数据（包含新特征）
example_data = {
    'srcIp': '172.26.153.169',
    'dstIp': '172.26.153.142',
    'srcPort': 45836,
    'dstPort': 80,
    'httpOpen': 'No',
    'tcpWindowSize': 490,
    'zeroWindow': 20,
    'duration': 200,
    'resPacketSplit': 800
}

# 预处理示例数据（排除新特征）
preprocessed_example = {
    'httpOpen': example_data['httpOpen'],
    'tcpWindowSize<=1024': 'Yes' if example_data['tcpWindowSize'] <= 1024 else 'No',
    'zeroWindow>10/min': 'Yes' if example_data['zeroWindow'] > 10 else 'No',
    'duration>300s': 'Yes' if example_data['duration'] > 300 else 'No',
    'resPacketSplit>1000p': 'Yes' if example_data['resPacketSplit'] > 1000 else 'No'
}

# 对示例数据进行编码
encoded_example = {}
for col, value in preprocessed_example.items():
    encoded_example[col] = feature_encoders[col].transform([value])[0]

# 转换为 DataFrame 以便输入模型
example_df = pd.DataFrame([encoded_example])

# 修改：当 httpOpen 为 'No' 时，直接判定为 'NORMAL'
if example_data['httpOpen'] == 'No':
    decoded_prediction = 'NORMAL'
else:
    # 进行预测
    prediction = model.predict(example_df)
    # 将预测结果解码
    decoded_prediction = 'ABNORMAL' if prediction[0] == 1 else 'NORMAL'

print(f"示例数据的预测结果: {decoded_prediction}")

# 输出结果
plt.figure(figsize=(14, 8))
tree.plot_tree(model, filled=True, feature_names=['httpOpen', 'tcpWindowSize<=1024', 'zeroWindow>10/min', 'duration>300s', 'resPacketSplit>1000p'])
plt.show()

# 创建 PMML 管道（排除新特征）
pipeline = PMMLPipeline([("classifier", model)])

# 将模型导出为 PMML 文件
sklearn2pmml(pipeline, "cart_model2.pmml", with_repr=True)
print("模型已导出为 PMML 文件")
    
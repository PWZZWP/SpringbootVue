import pandas as pd
from sklearn.impute import SimpleImputer
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeClassifier
from sklearn.metrics import accuracy_score

# 从 CSV 文件读取数据
df = pd.read_csv('cart_dataset.csv')

# 缺失值处理
imputer = SimpleImputer(strategy='most_frequent')
df[['A', 'B', 'C', 'D', 'E']] = imputer.fit_transform(df[['A', 'B', 'C', 'D', 'E']])

# 特征编码
le = LabelEncoder()
df['label'] = le.fit_transform(df['label'])
feature_encoders = {}
for col in ['A', 'B', 'C', 'D', 'E']:
    feature_encoders[col] = LabelEncoder()
    df[col] = feature_encoders[col].fit_transform(df[col])

# 划分训练集和测试集
X = df[['A', 'B', 'C', 'D', 'E']]
y = df['label']
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# 构建 CART 决策树模型
model = DecisionTreeClassifier(criterion = 'gini',random_state=0,max_depth=4)
model.fit(X_train, y_train)

# 模型预测
y_pred = model.predict(X_test)

# 评估模型
accuracy = accuracy_score(y_test, y_pred)
print(f"模型准确率: {accuracy}")
print("训练集准确率：",model.score(X_train, y_train)*100,"%")

# 示例数据
example_data = {
    'A': '是',
    'B': '是',
    'C': '是',
    'D': '是',
    'E': '是'
}

# 对示例数据进行编码
encoded_example = {}
for col, value in example_data.items():
    encoded_example[col] = feature_encoders[col].transform([value])[0]

# 转换为 DataFrame 以便输入模型
example_df = pd.DataFrame([encoded_example])

# 进行预测
prediction = model.predict(example_df)
# 将预测结果解码
decoded_prediction = 'ABNORMAL' if prediction[0] == 1 else 'NORMAL'
print(f"示例数据的预测结果: {decoded_prediction}")
    

#输出结果
import matplotlib.pyplot as plt
plt.figure(figsize=(14,8))
from sklearn import tree
tree.plot_tree(model,filled=True,feature_names=['A','B','C','D','E'])
plt.show()
    
#导入导出PMML文件所需的库
from sklearn2pmml import PMMLPipeline,sklearn2pmml

#创建PMML管道
pipeline = PMMLPipeline([("classifier",model)])

#将模型导出为PMML文件
sklearn2pmml(pipeline, "cart_model.pmml",with_repr=True)
print("模型已导出为PMML文件")
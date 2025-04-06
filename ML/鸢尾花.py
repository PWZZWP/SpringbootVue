#数据集的导入
import pandas as pd
import sklearn
from sklearn.datasets import load_iris

iris = load_iris()
# print(iris.feature_names)
# df = pd.DataFrame(iris.data, columns=iris.feature_names)#创建DataFrame对象，由行和列组成，每列可以有不同的数据类型
# #包含数据集的数据，列名为特征名
# # print(df[:5])#切片操作[:n],表示从索引0开始，选取到n-1的数据
# # print()
# # print(len(df))
# # print(df)

#划分数据集
from sklearn.model_selection import train_test_split
iris_x = iris.data#特征二维数组，每行代表一个样本，每列代表一个特征
iris_y = iris.target#标签一维数组，每行代表一个样本的标签
# print(iris_x)
# print(iris_y)
# print('数据集规格大小：',iris_x.shape)
# print('标签有：',iris.target_names)
# print('数据属性有：',iris.feature_names)

#train_test_split()是从样本中随机按比例选取train data和test data
#test_size测试集占样本比例
#y_train：训练集的标签数据，与X——train对应，用于指导模型学习
X_train, X_test ,y_train ,y_test = train_test_split(iris_x,iris_y,test_size=0.3)
# print(X_train)
# print(X_test)
# print(y_train)
# print(y_test)
# print('训练集大小：',len(X_train))
# print('测试集大小：',len(X_test))


#sklearn库中模型的训练，测试，推理
'''
clf=XXXX()#实例化一个模型（CART）
clf.fit(X_train,y_train)#用训练中的数据训练（拟合）模型
pred = clf.predict(X_test)#用测试集中的输入数据输入到训练好的模型中，得到模型的预测结果
score_train = clf.score(X_train,y_train)计算模型在训练集上的精度，将X_train传入，做预测并与y_train对比，计算正确率
score_test = clf.score(X_test,y_test)计算模型在测试集上的精度
'''
from sklearn.naive_bayes import GaussianNB
clf = GaussianNB()
clf.fit(X_train,y_train)#拟合/训练
pre = clf.predict(X_test)#预测

print ('真实值',y_test)
print ('预测值',pre)

print('Gaussian Train score:{:.4f}'.format(clf.score(X_train,y_train)))
print('Gaussian Test score:{:.4f}'.format(clf.score(X_test,y_test)))

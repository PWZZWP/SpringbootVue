from sklearn import datasets
from openscoring import Openscoring
iris = datasets.load_iris()

X = iris.data
y = iris.target

feature_names = iris.feature_names

#数据集划分
from sklearn.model_selection import train_test_split
X_train,X_test,y_train,y_test = train_test_split(X,y,test_size=0.2)

#模型引入，训练，测试
from sklearn.tree import DecisionTreeClassifier
clf = DecisionTreeClassifier(criterion = 'gini',random_state=0,max_depth=3)
clf.fit(X_train,y_train)
y_pred = clf.predict(X_test)

print(y_pred)
#计算正确率
print("测试集准确率:",clf.score(X_test,y_test))
print("训练集准确率：",clf.score(X_train,y_train)*100,"%")

#输出结果
import matplotlib.pyplot as plt
plt.figure(figsize=(14,8))
from sklearn import tree
tree.plot_tree(clf,filled=True,feature_names=feature_names)
plt.show()

#导入导出PMML文件所需的库
from sklearn2pmml import PMMLPipeline,sklearn2pmml

#创建PMML管道
pipeline = PMMLPipeline([("classifier",clf)])

#将模型导出为PMML文件
sklearn2pmml(pipeline,"iris_decision_tree.pmml",with_repr=True)
print("PMML文件已导出至当前目录！")

#使用openscoring进行预测
try:
    #启动本地的openscoring服务，确保openscoring服务已启动
    os = Openscoring("http://localhost:6666/openscoring")

    #部署PMML模型
    os.deployFile("IrisDecisionTree","iris_decision_tree.pmml")

    #准备一个样本进行预测,feature_names[0]是第一个特征的名称，X_test[0][0]是测试集样本中第一个样本的第一个特征的值

    sample = {
        feature_names[0]: X_test[0][0],
        feature_names[1]: X_test[0][1],
        feature_names[2]: X_test[0][2],
        feature_names[3]: X_test[0][3]
    }

    #进行预测
    result = os.evaluate("IrisDecisionTree",sample)
    print("使用PMML模型进行预测结果："+result)

    #取消部署模型
    os.undeploy("IrisDecisionTree")
except Exception as e:
    print(f"使用PMML文件进行预测是出现错误：{e}")
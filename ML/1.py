import numpy
import pandas as pd
import sklearn

#采用pandas库的DataFrame格式构造数据集
data = pd.DataFrame({
    '学号':[1,2,3,4,5,6,7,8,8],
    '身高':[172.0,167.0,169.0,171.0,170.0,168.0,165.0,163.0,163.0],
    '体重':[70,62,65,67,66,64,61,59,59]
})

print(data.duplicated())

data.drop_duplicates(subset=['学号'],keep='first',inplace=True)

print(data)

#data=data.dropna();删除包含nan值的行
#data3=data.fillna(method='bfill');用前一行的对应值填充nan值
#data4=data.fillna(method='ffill');用后一行的对应值填充nan值
#data5=data['身高'].fillna(data['身高']).mean(),inplace=True)使用身高列的平均值填充nan值
#将身高>200的数据视为异常数据print(data['身高']>200)
#print(any(data['身高']>200))寻找一系列布尔值中是否有True.存在任意一个返回True.
#找到小于200身高的最大值，然后将异常值用最大值填充
#idx=data['身高']<200 #获取身高小于200的数据的布尔索引

#data['身高'][idx] #一布尔值作为索引，获取身高小于200的那些数据

#new_value = data['身高'][idx].max() #获取身高小于200的最大值

#用loc进行索引，data.loc[['身高']>200,'身高'] = new_value #将身高大于200的行的身高列的值替换为new_value

import numpy as np
X= np.array([[2,2,-1],
           [1,2,-2],
           [0,-2,2]])

from sklearn import preprocessing
# scaler = preprocessing.MinMaxScaler()
# X_processing = scaler.fit_transform(X)
# print(X_processing)

scaler = preprocessing.StandardScaler()
X_processing = scaler.fit_transform(X)
print(X_processing)
X_processing.var()#方差为1
X_processing.mean()#均值接近0
print(X_processing.var())
print(X_processing.mean())
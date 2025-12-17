from django.urls import path, include
from . import views
from rest_framework import routers

router = routers.DefaultRouter()
router.register('Post', views.BlogImage)

urlpatterns = [
    path('', views.post_list, name='post_list'),
    path('gallery/', views.photo_gallery, name='photo_gallery'),
    path('trends/', views.trends, name='trends'),
    path('api/trend-data/', views.trend_data, name='trend_data'),
    # path('post/<int:pk>/', views.post_detail, name='post_detail'),
    # path('post/new/', views.post_new, name='post_new'),
    # path('post/<int:pk>/edit/', views.post_edit, name='post_edit'),
    # path('js_test/', views.js_test, name='js_test'),
    path('api_root/', include(router.urls)),
]
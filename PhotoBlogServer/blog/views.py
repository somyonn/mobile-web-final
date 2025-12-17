from django.shortcuts import render
from django.utils import timezone
from .models import Post
from django.shortcuts import render, get_object_or_404
from rest_framework import viewsets
from rest_framework.decorators import api_view
from rest_framework.response import Response
from .serializer import PostSerializer
from django.db.models import Count, Q
from datetime import datetime, timedelta
from collections import defaultdict
import json

# Create your views here.
def post_list(request):
    """메인 랜딩 페이지"""
    # 최근 포스트 몇 개만 가져오기 (선택사항)
    recent_posts = Post.objects.filter(published_date__lte=timezone.now()).order_by('-published_date')[:6]
    
    # 통계 정보
    total_posts = Post.objects.filter(published_date__isnull=False).count()
    
    # 가장 오래된 포스트 날짜 (금연 시작일)
    earliest_post = Post.objects.filter(published_date__isnull=False).order_by('published_date').first()
    days_count = 0
    if earliest_post:
        days_count = (timezone.now().date() - earliest_post.published_date.date()).days
    
    context = {
        'recent_posts': recent_posts,
        'total_posts': total_posts,
        'days_count': days_count,
    }
    return render(request, 'blog/post_list.html', context)

def post_detail(request, pk):
    post = get_object_or_404(Post, pk=pk)
    return render(request, 'blog/post_detail.html', {'post': post})

def photo_gallery(request):
    """년/월/일 필터링이 있는 사진 갤러리"""
    posts = Post.objects.filter(published_date__isnull=False).order_by('-published_date')
    
    # 필터링 파라미터
    year = request.GET.get('year')
    month = request.GET.get('month')
    day = request.GET.get('day')
    
    if year:
        posts = posts.filter(published_date__year=year)
    if month:
        posts = posts.filter(published_date__month=month)
    if day:
        posts = posts.filter(published_date__day=day)
    
    # 사용 가능한 년/월/일 목록
    all_posts = Post.objects.filter(published_date__isnull=False)
    
    # 년도 목록 (중복 제거)
    years = sorted(set(all_posts.values_list('published_date__year', flat=True)), reverse=True)
    available_years = [type('obj', (object,), {'year': y})() for y in years]
    
    # 월 목록 (중복 제거)
    months = sorted(set(all_posts.values_list('published_date__month', flat=True)), reverse=True)
    available_months = [type('obj', (object,), {'month': m})() for m in months]
    
    # 일 목록 (중복 제거)
    days = sorted(set(all_posts.values_list('published_date__day', flat=True)), reverse=True)
    available_days = [type('obj', (object,), {'day': d})() for d in days]
    
    context = {
        'posts': posts,
        'available_years': available_years,
        'available_months': available_months,
        'available_days': available_days,
        'selected_year': year,
        'selected_month': month,
        'selected_day': day,
    }
    return render(request, 'blog/photo_gallery.html', context)

def trends(request):
    """금연 추세 그래프 페이지"""
    return render(request, 'blog/trends.html')

@api_view(['GET'])
def trend_data(request):
    """그래프 데이터를 제공하는 API"""
    period = request.GET.get('period', 'hourly')  # hourly, daily, monthly
    
    posts = Post.objects.filter(published_date__isnull=False).order_by('published_date')
    
    data = {}
    labels = []
    values = []
    
    if period == 'hourly':
        # 시간별 (0시~23시)
        hourly_count = defaultdict(int)
        for post in posts:
            hour = post.published_date.hour
            hourly_count[hour] += 1
        
        for hour in range(24):
            labels.append(f"{hour:02d}시")
            values.append(hourly_count[hour])
            
    elif period == 'daily':
        # 요일별 (일~토)
        day_names = ['일요일', '월요일', '화요일', '수요일', '목요일', '금요일', '토요일']
        daily_count = defaultdict(int)
        
        for post in posts:
            day_of_week = post.published_date.weekday()
            daily_count[day_of_week] += 1
        
        for day in range(7):
            labels.append(day_names[day])
            values.append(daily_count[day])
            
    elif period == 'monthly':
        # 30일간 추세
        thirty_days_ago = timezone.now() - timedelta(days=30)
        recent_posts = posts.filter(published_date__gte=thirty_days_ago)
        
        daily_count = defaultdict(int)
        for post in recent_posts:
            date_key = post.published_date.date()
            daily_count[date_key] += 1
        
        # 날짜 순서로 정렬
        sorted_dates = sorted(daily_count.keys())
        for date_key in sorted_dates:
            labels.append(date_key.strftime('%m/%d'))
            values.append(daily_count[date_key])
    
    data = {
        'labels': labels,
        'values': values,
        'period': period
    }
    
    return Response(data)

class BlogImage(viewsets.ModelViewSet):
    queryset = Post.objects.all()
    serializer_class = PostSerializer
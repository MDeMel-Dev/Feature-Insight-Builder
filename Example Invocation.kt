featureInsightBuilder.buildInsight(
      targetView = binding.manageBrandsCount,
      isViewStateReadyCheck = { viewModel.isLoading.get() },
      insightComposableAreaSpecs = InsightComposableAreaSpecs(
        areaWidth = 343,
        areaHeight = 181
      )
    ) {
      Column(
        modifier = Modifier
          .width(343.dp)
          .height(181.dp)
          .background(Color.White)
      ) {}
    }

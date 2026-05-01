
import userRoutes from './user'
import creationRoutes from './creation'
import taxonomyRoutes from './taxonomy'
import searchRoutes from './search'
import personalRoutes from './personal'
import adminRoutes from './admin'

export default [
  ...userRoutes,
  ...creationRoutes,
  ...taxonomyRoutes,
  ...searchRoutes,
  ...personalRoutes,
  ...adminRoutes
]
